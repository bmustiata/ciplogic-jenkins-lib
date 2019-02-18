def call(config) {
    if (!(config instanceof Map)) {
        config = [
            dockerTag: config,
            publishLocal: true,
        ]
    }

    if (config.pushDockerSami) {
        sh """
            docker tag ${config.dockerTag} ge-registry:80/${config.dockerTag}
            docker push ge-registry:80/${config.dockerTag}
        """
    }

    if (config.pushDockerHub) {
        withCredentials([file(credentialsId: 'DOCKERHUB_LOGIN', variable: 'DOCKERHUB_LOGIN')]) {
            sh """
                cat \$DOCKERHUB_LOGIN | docker login --username germaniumhq --password-stdin
                docker push ${config.dockerTag}
            """
        }
    }
}

