/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
    def filename = config?.file ?: "Dockerfile"
    def buildArguments = config?.build_args ?: []
    def tags = config?.tags ?: []
    def networks = config?.networks ?: null
    def noCache = config?.no_cache ?: false

    def stringNetwork = ''
    for (String network: networks) {
        stringNetwork += "--network ${network} "
    }

    def stringNoCache = ''
    if (noCache) {
        stringNoCache += "--no-cache "
    }

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
        docker build ${stringNetwork}${stringArguments}${stringTags}-f \$(basename ${filename}) .
    """

    print "Going to run:\n$script"

    sh script
}

