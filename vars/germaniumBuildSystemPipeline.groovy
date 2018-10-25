def call(config) {
    properties([
        safeParameters(this, [
            booleanParam(name: 'REFRESH_BUILD', defaultValue: false,
                    description: 'Should all the base images be repulled, and caches ignored.')
        ])
    ])

    safeParametersCheck(this)

    def runDockerBuild = { imageName, folderName, useCache ->
        node {
            deleteDir()
            checkout scm

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

    stage('Create Containers') {
        runParallelTasks(config.platformImages, runDockerBuild, !REFRESH_BUILD)
    }

    stage('Push Containers') {
        runParallelTasks(config.platformImages, runDockerPush, true)
    }
}
