def call(config) {
    def containerName = config?.name
    def imageName = config?.image

    if (!containerName) {
        throw new IllegalArgumentException('You need to pase the container name as `name`')
    }

    if (!imageName) {
        throw new IllegalArgumentException('You need to pass the image name as `image`')
    }

    sh """
        docker commit ${containerName} ${imageName}
    """.trim()
}

