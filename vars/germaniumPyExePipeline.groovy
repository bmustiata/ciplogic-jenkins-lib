def call(config) {
    properties([
        safeParameters(config.that, [
            booleanParam(name: 'RUN_MYPY_CHECKS', defaultValue: true,
                    description: 'Run the mypy type checks.'),
            booleanParam(name: 'RUN_FLAKE8_CHECKS', defaultValue: true,
                    description: 'Run the flake8 linting.')
        ])
    ])

    safeParametersCheck(config.that)

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
                if (RUN_MYPY_CHECKS) {
                    sh """
                        mypy .
                    """
                }

                if (RUN_FLAKE8_CHECKS) {
                    sh """
                        flake8 .
                    """
                }
            }
        }
    }

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

    stage('Archive') {
        node {
            def parallelArchiving = [:]

            config.binaries.each { platformName, platformConfig ->
                parallelArchiving[platformName] = {
                    node {
                        docker.image(platformConfig.dockerTag)
                              .inside {
                            sh """
                                pwd
                                ls -la
                            """

                            archiveArtifacts(
                                artifacts: platformConfig.exe,
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

