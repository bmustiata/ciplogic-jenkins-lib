def call(config) {
    if (!(config instanceof Map)) {
        config = [playbook: config]
    }

    runContainers tools: [
        ansible: {
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

                    cd /src

                    export ANSIBLE_HOST_KEY_CHECKING=False
                    ansible-playbook --check -i /tmp/ANSIBLE_INVENTORY ${config.playbook}
                """
            }
        }
    ]
}

