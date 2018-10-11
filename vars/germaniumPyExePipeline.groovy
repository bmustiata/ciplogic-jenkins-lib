def call(config) {
    def runMyPyChecks = config.containsKey("runMyPy") ? config.runMyMy : true
    def runFlake8Checks = config.containsKey("runFlake8") ? config.runFlake8 : true

    properties([
        safeParameters(this, [
            booleanParam(name: 'RUN_MYPY_CHECKS', defaultValue: runMyPyChecks,
                    description: 'Run the mypy type checks.'),
            booleanParam(name: 'RUN_FLAKE8_CHECKS', defaultValue: runFlake8Checks,
                    description: 'Run the flake8 linting.')
        ])
    ])

    safeParametersCheck(this)

    // -------------------------------------------------------------------
    // Static type checking for the project.
    // -------------------------------------------------------------------
    stage('Type Check') {
        if (!RUN_MYPY_CHECKS && !RUN_FLAKE8_CHECKS) {
            return;
        }

        node {
            deleteDir()
            checkout scm

            docker.build("mypy_${env.BUILD_ID}",
                         '-f Dockerfile.py.build .')
                  .inside("-v ${pwd()}:/src") {

                def parallelChecks = [:]

                if (RUN_MYPY_CHECKS) {
                    parallelChecks."mypy" = {
                        // We need to run it in the /src folder to pick the setup.cfg if present.
                        sh """
                            cd /src
                            export MYPYPATH=/src/stubs
                            mypy .
                        """
                    }
                }

                if (RUN_FLAKE8_CHECKS) {
                    parallelChecks."flake8" = {
                        // We need to run it in the /src folder to pick the setup.cfg if present.
                        sh """
                            cd /src
                            flake8 .
                        """
                    }
                }

                parallel(parallelChecks)
            }
        }
    }

    // -------------------------------------------------------------------
    // Creation of the actual binaries per each platform, using GBS.
    // -------------------------------------------------------------------
    stage('Build') {
        def parallelBuilds = [:]

        config.binaries.each { platformName, platformConfig ->
            def gbsPath = platformConfig.gbs ?: "/"

            parallelBuilds[platformName] = {
                node {
                    deleteDir()
                    checkout scm

                    // do the version manager stamping
                    versionManager()

                    if (platformConfig.extraSteps) {
                        platformConfig.extraSteps()
                    }

                    dockerBuild(
                        file: ".${gbsPath}Dockerfile",
                        build_args: ["GBS_PREFIX=${gbsPath}"],
                        tags: [platformConfig.dockerTag]
                    )
                }
            }
        }

        parallel(parallelBuilds)
    }

    // -------------------------------------------------------------------
    // Archival of the binaries.
    // -------------------------------------------------------------------
    stage('Archive') {
        node {
            def parallelArchiving = [:]

            config.binaries.each { platformName, platformConfig ->
                parallelArchiving[platformName] = {
                    docker.image(platformConfig.dockerTag).inside {
                        def exeName = platformConfig.exe.replaceAll("^.*/", "")

                        // W/A for archiveArtifacts that can't copy outside workspace even
                        // in docker containers.
                        sh """
                            mkdir -p '${pwd()}/_archive'
                            cp '${platformConfig.exe}' '${pwd()}/_archive/${exeName}'
                        """

                        archiveArtifacts(
                            artifacts: "_archive/${exeName}",
                            fingerprint: true
                        )
                    }
                }
            }

            parallel(parallelArchiving)
        }
    }

    // -------------------------------------------------------------------
    // Create local tool containers
    // -------------------------------------------------------------------
    if (config.binaries.find({platformName, platformConfig -> platformConfig.dockerToolContainer})) {
        stage('Local Docker Container') {
            node {
                def parallelPublish = [:]

                config.binaries.each { platformName, platformConfig ->
                    def dockerToolContainer = platformConfig.dockerToolContainer ?: false

                    if (!dockerToolContainer) {
                        return
                    }

                    parallelPublish[platformName] = {
                        dockerRm containers: [platformConfig.dockerTag]
                        dockerRun image: platformConfig.dockerTag,
                            name: platformConfig.dockerTag,
                            command: "ls"
                    }
                }

                parallel(parallelPublish)
            }
        }
    }

    // -------------------------------------------------------------------
    // Docker publish
    // -------------------------------------------------------------------
    if (config.binaries.find({platformName, platforConfig -> platforConfig.dockerPublish})) {
        stage('Publish Docker') {
            node {
                def parallelPublish = [:]

                config.binaries.each { platformName, platformConfig ->
                    def dockerPublish = platformConfig.dockerPublish ?: false

                    if (!dockerPublish) {
                        return
                    }

                    parallelPublish[platformName] = {
                        dpush platformConfig.dockerTag
                    }
                }

                parallel(parallelPublish)
            }
        }
    }

    // -------------------------------------------------------------------
    // GermaniumHQ Downloads Publish
    // -------------------------------------------------------------------
    if (config.binaries.find({platformName, platforConfig -> platforConfig.publishDownloads})) {
        stage('Publish downloads.GermaniumHQ.com') {
            node {
                def parallelPublish = [:]

                config.binaries.each { platformName, platformConfig ->
                    def publishDownloads = platformConfig.publishDownloads ?: false

                    if (!publishDownloads) {
                        return
                    }

                    parallelPublish[platformName] = {
                        unarchive
                        ansiblePlay publishDownloads
                    }
                }

                parallel(parallelPublish)
            }
        }
    }
}

