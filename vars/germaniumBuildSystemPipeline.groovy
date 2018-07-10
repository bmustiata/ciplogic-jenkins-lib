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

    def runParallelBuilds = { imageMap, code ->
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
        runParallelBuilds(config.baseImages, runDockerBuild)
    }

    stage('Create Build Containers') {
        runParallelBuilds(config.runImages, runDockerBuild)
    }

    stage('Push docker containers') {
        def allImages = [:]
        allImages.putAll(config.baseImages)
        allImages.putAll(config.runImages)

        runParallelBuilds(allImages, runDockerPush)
    }
}
