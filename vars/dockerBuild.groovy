/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
    def fileName = config?.file

    if (!fileName) {
        filename = "Dockerfile"
    }

    sh """
        docker build -f $(basename ${filename}) $(dirname ${filename})
    """
}

