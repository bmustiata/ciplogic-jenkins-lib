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
                    node {
                        docker.image(platformConfig.dockerTag).inside {
                            def exeName = platformConfig.exe.replaceAll("^.*/", "")

                            // W/A for archiveArtifacts that can't copy outside workspace even
                            // in docker containers.
                            sh """
                                cp '${platformConfig.exe}' '${pwd()}/${exeName}'
                            """

                            archiveArtifacts(
                                artifacts: exeName,
                                fingerprint: true
                            )
                        }
                    }
                }
            }

            parallel(parallelArchiving)
        }
    }
}
