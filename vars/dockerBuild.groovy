/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
    def filename = config?.file

    if (!filename) {
        filename = "Dockerfile"
    }

    sh """
        docker build -f \$(basename ${filename}) \$(dirname ${filename})
    """
}

