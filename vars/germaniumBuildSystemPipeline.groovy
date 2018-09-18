def call(config) {
    def runDockerBuild = { imageName, folderName ->
        node {
            deleteDir()
            checkout scm

            docker.build(imageName, folderName)
        }
    }

    def runDockerPush = { imageName, folderName ->
        node {
            dpush imageName
        }
    }

    def runParallelTasks = { imageMap, code ->
        def parallelJobs = [:]

        imageMap.entrySet().each({e ->
            def image = e.key
            def folder = e.value

            parallelJobs[image] = {
                code(image, folder)
            }
        })

        parallel(parallelJobs)
    }

    stage('Create Base/Run Containers') {
        runParallelTasks(config.baseImages, runDockerBuild)
    }

    stage('Create Build Containers') {
        runParallelTasks(config.runImages, runDockerBuild)
    }

    stage('Push docker containers') {
        def allImages = [:]
        allImages.putAll(config.baseImages)
        allImages.putAll(config.runImages)

        runParallelTasks(allImages, runDockerPush)
    }
}
