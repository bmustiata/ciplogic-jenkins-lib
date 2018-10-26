@NonCPS
def parseUrl(String gitUrl) {
    def m = (gitUrl =~ /(.*?\:\/\/)?(.*?@)?(.+?)(\:.+)?\/.*/)

    if (!m.matches()) {
        throw new IllegalArgumentException("Unable to parse git url: ${gitUrl}")
    }

    def serverName = m.group(3)

    return serverName
}

def call(config) {
    if (!config || !config.repo) {
        throw new IllegalArgumentException("You need to pass a `repo` config argument where git is")
    }

    if (!(config.repo instanceof List)) {
        config.repo = [ config.repo ]
    }

    runContainers tools: [
        "git": {
            withCredentials([file(credentialsId: 'GITHUB_JENKINS_PUBLISH_KEY', variable: 'JENKINS_KEY')]) {
                try {
                    sh """
                        mkdir -p /germanium/.ssh
                        chmod 700 /germanium/.ssh
                        cp ${env.JENKINS_KEY} /germanium/.ssh/id_rsa
                        chmod 600 /germanium/.ssh/id_rsa

                    """

                    def gitPushParallel = [:]

                    config.repo.each { gitUrl ->
                        def serverName = parseUrl(gitUrl)

                        gitPushParallel."${serverName}" = {
                            sh """
                                ssh-keyscan ${serverName} >> /germanium/.ssh/known_hosts

                                git remote add ${serverName} ${gitUrl} || true

                                git push ${serverName} HEAD:master
                                git push ${serverName} --tags -f
                            """
                        }
                    }

                    parallel(gitPushParallel)


                } finally {
                    sh """
                        rm /germanium/.ssh/id_rsa
                    """
                }
            }
        }
    ]
}
