def call(config) {
    properties([
        safeParameters(this, [
            booleanParam(name: 'USE_DOCKER_CACHES', defaultValue: true,
                    description: 'Should the docker caching be used.')
        ])
    ])

    safeParametersCheck(this)

    def runDockerBuild = { imageName, folderName, useCache ->
        node {
            deleteDir()
            checkout scm

            if (!useCache) {
                sh """
                    docker pull \$(cat ${folderName}/Dockerfile | head -1 | cut -f2 -d\\ )
                """
            }

            docker.build(imageName, useCache ? folderName : "--no-cache ${folderName}")
        }
    }

    def runDockerPush = { imageName, folderName, useCache ->
        node {
            dpush imageName
        }
    }

    def runParallelTasks = { imageMap, code, useCache ->
        def parallelJobs = [:]

        imageMap.entrySet().each({e ->
            def image = e.key
            def folder = e.value

            parallelJobs[image] = {
                code(image, folder, useCache)
            }
        })

        parallel(parallelJobs)
    }

    stage('Fetch Base Images') {
        if (USE_DOCKER_CACHES) {
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

    stage('Create Base/Run Containers') {
        runParallelTasks(config.baseImages, runDockerBuild, USE_DOCKER_CACHES)
    }

    stage('Create Build Containers') {
        runParallelTasks(config.runImages, runDockerBuild, true)
    }

    stage('Push docker containers') {
        def allImages = [:]
        allImages.putAll(config.baseImages)
        allImages.putAll(config.runImages)

        runParallelTasks(allImages, runDockerPush, true)
    }
}
