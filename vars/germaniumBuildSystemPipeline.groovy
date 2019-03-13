def call(config) {
    properties([
        safeParameters(this, [
            booleanParam(name: 'REFRESH_BUILD', defaultValue: false,
                    description: 'Should all the base images be repulled, and caches ignored.')
        ])
    ])

    safeParametersCheck(this)

    def runDockerBuild = { images, folderName, useCache ->
        node {
            deleteDir()
            checkout scm

            def extraTags = images.drop(1)
                              .collect({tag -> "-t ${tag}"})
                              .join(" ")

            def buildArgs = folderName

            if (!useCache) {
                buildArgs = "--no-cache ${buildArgs}"
            }

            if (extraTags) {
                buildArgs = "${extraTags} ${buildArgs}"
            }

            docker.build(images[0], buildArgs)
        }
    }

    def runDockerPush = { images, folderName, useCache ->
        node {
            images.each { imageName ->
                dpush imageName
            }
        }
    }

    /**
     * Runs the `code` function for item in the imageMap, with
     * the key as a folder, and the value as a list of tags to
     * be published.
     */
    def runParallelTasks = { imageMap, code, useCache ->
        def parallelJobs = [:]

        echo "image map: ${imageMap}"

        imageMap.entrySet().each({e ->
            def folder = e.key
            def images = e.value

            if (!(images instanceof List)) {
                images = [ images ]
            }

            parallelJobs[folder] = {
                code.call(images, folder, useCache)
            }
        })

        parallel(parallelJobs)
    }

    stage('Prepare tooling') {
        ensureDockerTooling tools: "behave"
    }

    stage('Fetch Base Images') {
        if (!REFRESH_BUILD) {
            return
        }

        node {
            deleteDir()
            checkout scm

            // this should actually scan the map and get the values, but meh.
            def imagesToPull = sh([
                script: """
                    cat */Dockerfile | grep FROM | cut -f2 -d\\ | grep -v germaniumhq | sort -u
                """,
                returnStdout: true]).trim().split()

            def imagesParallelTasks = [:]

            imagesToPull.each { imageName ->
                imagesParallelTasks."${imageName}" = {
                    sh """
                        docker pull ${imageName}
                    """
                }
            }

            parallel(imagesParallelTasks)
        }
    }

    config.platformImages.eachWithIndex { containersMap, index ->
        stage("Create Containers ${index}") {
                runParallelTasks(containersMap,
                                 runDockerBuild,
                                 !REFRESH_BUILD)
        }
    }

    stage('Test Containers') {
        node {
            deleteDir()
            checkout scm

            // run the container with the same groups, so docker can execute normally
            def userGroupIds = sh(script: "id -G", returnStdout: true)
                .trim()
                .split()
                .collect({
                    groupId -> "--group-add ${groupId}"
                }).join(" ")

            runContainers tools: [
                behave: [
                    docker_params: "${userGroupIds} -v /var/run/docker.sock:/var/run/docker.sock",
                    inside : {
                        sh """
                            id
                            behave
                        """
                    }
                ]
            ]
        }
    }

    stage('Push Containers') {
        runParallelTasks(config.platformImages.collectEntries({ it }),
                         runDockerPush,
                         true)
    }

    // -------------------------------------------------------------------
    // git servers push publish
    // -------------------------------------------------------------------
    if (config.repo && (isMasterBranch() || isTagVersion())) {
        stage('Publish git repo(s)') {
            node {
                deleteDir()
                checkout scm

                if (isMasterBranch()) {
                    publishGit(config)
                }

                if (isTagVersion()) {
                    publishGitTags(config)
                }
            }
        }
    }
}

