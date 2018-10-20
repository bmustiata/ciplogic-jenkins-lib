def call(config) {
    def runMyPyChecks = config.containsKey("runMyPy") ? config.runMyMy : true
    def runFlake8Checks = config.containsKey("runFlake8") ? config.runFlake8 : true
    def publishPublicPyPi = config.containsKey("publishPublicPyPi") ? config.publishPublicPyPi: false

    properties([
        safeParameters(this, [
            booleanParam(name: 'RUN_MYPY_CHECKS', defaultValue: runMyPyChecks,
                    description: 'Run the mypy type checks.'),
            booleanParam(name: 'RUN_FLAKE8_CHECKS', defaultValue: runFlake8Checks,
                    description: 'Run the flake8 linting.'),
            booleanParam(name: 'FORCE_PYPI_PUBLISH', defaultValue: false,
                    description: 'Forces pypi publishing even if not on master.')
        ])
    ])

    safeParametersCheck(this)

    // -------------------------------------------------------------------
    // Ensure tooling is available
    // -------------------------------------------------------------------
    ensureDockerTooling stage: "Tooling",
        tools: [ [
                name: "mypy",
                when: RUN_MYPY_CHECKS
            ], [
                name: "flake8",
                when: RUN_FLAKE8_CHECKS
            ], [
                name: "ansible",
                when: config.publishAnsiblePlay && isTagVersion()
            ], [
                name: "version-manager"
            ]
        ]

    // -------------------------------------------------------------------
    // Quality gate checking for the project.
    // -------------------------------------------------------------------
    stage('Checks') {
        node {
            deleteDir()
            checkout scm

            runContainers tools: [
                    "mypy": [
                        when: RUN_MYPY_CHECKS,
                        inside: {
                            sh "export MYPYPATH=./stubs; mypy ."
                        }
                    ],

                    "flake8": [
                        when: RUN_FLAKE8_CHECKS,
                        inside: {
                            sh "flake8 ."
                        }
                    ]
                ]
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
    // Run post build steps, i.e. integration tests
    // -------------------------------------------------------------------
    if (config.postBuild) {
        config.postBuild()
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
    // Publish on pypi
    // -------------------------------------------------------------------
    if (isTagVersion() && config.binaries.find({platformName, platform -> platform.publishPypi})) {
        stage('PiPy Publish') {
            node {
                def parallelPublish = [:]

                config.binaries.each { platformName, platformConfig ->
                    def publishPypiType = platformConfig.publishPypi ?: false

                    if (!publishPypiType) {
                        return
                    }

                    parallelPublish[platformName] = {
                        docker.image(platformConfig.dockerTag).inside {
                            publishPypi(publishPypiType)
                        }
                    }
                }

                parallel(parallelPublish)
            }
        }
    }

    // -------------------------------------------------------------------
    // GermaniumHQ Downloads Publish
    // -------------------------------------------------------------------
    stage('Publish on GermaniumHQ') {
        node {
            deleteDir()
            checkout scm

            ansiblePlay when: config.publishAnsiblePlay && isTagVersion(),
                inside: {
                    unarchive mapping: ["_archive/": "."]

                    sh """
                        export ANSIBLE_HOST_KEY_CHECKING=False
                        ansible-playbook -i /tmp/ANSIBLE_INVENTORY ${config.publishAnsiblePlay}
                    """
                }
        }
    }

}

