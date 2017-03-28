/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
    print config

    def filename = config?.file ?: "Dockerfile"
    def buildArguments = config?.build_args ?: []

    def stringArguments = ''
    buildArguments.each({
        stringArguments = stringArguments + "--build-arg='" + it + "' "
    })
    print stringArguments

    def script = """
        cd \$(dirname ${filename})
        docker build ${stringArguments} -f \$(basename ${filename}) .
    """

    print "Going to run:\n$script"

    sh script
}

