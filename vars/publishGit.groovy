def call(projectUrl) {
    runContainers tools: [
        "git": {
            withCredentials([file(credentialsId: 'GITHUB_JENKINS_PUBLISH_KEY', variable: 'JENKINS_KEY')]) {
                try {
                    sh """
                        mkdir -p ~/.ssh
                        cp ${env.JENKINS_KEY} ~/.ssh/id_rsa
                        chmod 600 ~/.ssh/id_rsa

                        ssh-keyscan github.com >> ~/.ssh/known_hosts

                        git remote add github ${projectUrl} || true

                        git push github HEAD:master
                        git push github --tags
                    """
                } finally {
                    sh """
                        rm /germanium/.ssh/id_rsa
                    """
                }
            }
        }
    ]
}
