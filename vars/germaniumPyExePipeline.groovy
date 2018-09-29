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
                        sh """
                            mypy .
                        """
                    }
                }

                if (RUN_FLAKE8_CHECKS) {
                    parallelChecks."flake8" = {
                        sh """
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
            parallelBuilds[platformName] = {
                node {
                    deleteDir()
                    checkout scm

                    dockerBuild(
                        file: ".${platformConfig.gbs}Dockerfile",
                        build_args: ["GBS_PREFIX=${platformConfig.gbs}"],
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
                        docker.image(platformConfig.dockerTag)
                              .inside {
                            dir("/") {
                                archiveArtifacts(
                                    artifacts: platformConfig.exe.substring(1),
                                    fingerprint: true
                                )
                            }
                        }
                    }
                }
            }

            parallel(parallelArchiving)
        }
    }
}

