/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
    def filename = config?.file ?: "Dockerfile"
    def buildArguments = config?.build_args ?: []
    def tags = config?.tags ?: []

    def stringArguments = ''
    for (int i = 0; i < buildArguments.size(); i++) {
        stringArguments += "--build-arg='" + buildArguments.get(i) + "' "
    }

    def stringTags = ''
    for (int i = 0; i < tags.size(); i++) {
        stringTags += "-t " + tags.get(i) + " "
    }

    def script = """
        cd \$(dirname ${filename})
        docker build ${stringArguments}${stringTags}-f \$(basename ${filename}) .
    """

    print "Going to run:\n$script"

    sh script
}

