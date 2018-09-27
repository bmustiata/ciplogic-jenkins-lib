def call(config) {
    stage('Type Check') {
        node {
            deleteDir()
            checkout scm

            docker.build("mypy_${env.BUILD_ID}",
                         '-f Dockerfile.py.build .')
                  .inside("-v ${pwd()}:/src")
            {
                sh """
                    cd /src
                    mypy application.py
                """
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

