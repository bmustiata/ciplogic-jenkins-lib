def call(config) {
    def containers = config.containers ? config.containers : []
    def images = config.images ? config.images : []

    containers.each { container ->
        sh """
            docker rm -f -v ${container} || true
        """
    }

    images.each { image ->
        sh """
            docker rmi ${image} || true
        """
    }
}
