def call(projectUrl) {
    runContainers tools: [
        "git": {
            withCredentials([file(credentialsId: 'GITHUB_JENKINS_PUBLISH_KEY', variable: 'JENKINS_KEY')]) {
                try {
                    sh """
                        mkdir -p /germanium/.ssh
                        chmod 700 /germanium/.ssh
                        cp ${env.JENKINS_KEY} /germanium/.ssh/id_rsa
                        chmod 600 /germanium/.ssh/id_rsa

                        ssh-keyscan github.com >> /germanium/.ssh/known_hosts

                        git remote add github ${projectUrl} || true

                        git push github HEAD:master
                        git push github --tags -f
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
