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
                when: config.publishAnsiblePlay && env.BRANCH_NAME == "master"
            ]
        ]

    // -------------------------------------------------------------------
    // Quality gate checking for the project.
    // -------------------------------------------------------------------
    runContainers stage: "Checks",
        tools: [
            "mypy": [
                when: RUN_MYPY_CHECKS,
                inside: {
                    sh "cd /src; export MYPYPATH=/src/stubs; mypy ."
                }
            ],

            "flake8": [
                when: RUN_FLAKE8_CHECKS,
                inside: {
                    sh "cd /src; flake8 ."
                }
            ]
        ]

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
    //if (config.publishAnsiblePlay && env.BRANCH_NAME == "master") {
    ansiblePlay stage: "Publish on GermaniumHQ",
        when: config.publishAnsiblePlay && env.BRANCH_NAME == "master",
        inside: {
            unarchive mapping: ["_archive/": "."]

            sh """
                cd /src
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook --check -i /tmp/ANSIBLE_INVENTORY ${config.publishAnsiblePlay}
            """
        }
}

