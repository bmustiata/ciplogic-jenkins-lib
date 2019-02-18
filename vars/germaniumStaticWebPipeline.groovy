def call(config) {
    ensureDockerTooling stage: "Tooling",
        tools: [ [
                name: "version-manager"
            ]
        ]

    stage('Test') {
        node {
            checkoutWithVersionManager()

            gbs().test(
                platform: "node:8",
                dockerTag: 'not-really-needed',
            )
        }
    }

    node {
        stage('Build Sources') {
            checkoutWithVersionManager()

            gbs().build(
                platform: "node:8",
                dockerTag: 'not-really-needed',
            ).inside {
                sh """
                    cp -R /src/dist ./dist
                """
            }
        }

        stage('Create Container') {
            docker.build(config.dockerTag)
        }
    }

    if (config.repo && (isMasterBranch() || isTagVersion())) {
        stage('Publish git') {
            node {
                deleteDir()
                checkout scm

                if (isMasterBranch()) {
                    publishGit(config)
                }

                if (isTagVersion()) {
                    publishGitTags(config)
                }
            }
        }
    }

    stage('Docker Push') {
        if (isMasterBranch() || isTagVersion()) {
            node {
                dpush(config)
            }
        }
    }
}
