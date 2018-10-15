def call(config) {
    if (!(config instanceof Map)) {
        config = [inside: config]
    }

    runContainers stage: config.stage,
        tools: [
            ansible: [
                when: config.when,
                inside: {
                    sh """
                        mkdir -p /germanium/.ssh
                    """

                    withCredentials([
                            file(credentialsId: 'ANSIBLE_INVENTORY', variable: 'ANSIBLE_INVENTORY'),
                            file(credentialsId: 'ANSIBLE_SSH_KEY', variable: 'ANSIBLE_SSH_KEY')]) {
                        sh """
                            cp ${env.ANSIBLE_INVENTORY} /tmp/ANSIBLE_INVENTORY
                            cp ${env.ANSIBLE_SSH_KEY} /germanium/.ssh/id_rsa

                            chmod 664 /tmp/ANSIBLE_INVENTORY
                            chmod 600 /germanium/.ssh/id_rsa
                        """

                        config.inside()
                    }
                }
            ]
        ]
}

