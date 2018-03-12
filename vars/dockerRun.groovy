def call(config) {
    def image = config?.image
    def command = config?.command ?: ''

    def env = config?.env ?: []
    def links = config?.links ?: []
    def ports = config?.ports ?: []
    def volumes = config?.volumes ?: []
    def remove = config?.remove ?: false
    def name = config?.name ?: ''
    def privileged = config?.privileged ?: false
    def networks = config?.networks ?: []

    if (!image) {
        print 'You need to specify what image to run, with image:...'
        throw new IllegalArgumentException('Missing `image` name')
    }

    def containerName = ''
    if (name) {
        containerName = "--name '${name}' "
    }

    def stringNetwork = ''
    for (String network: networks) {
        stringNetwork = "--network ${network} "
    }

    def stringEnv = ''
    for (int i = 0; i < env.size(); i++) {
        stringEnv += "-e '" + env.get(i) + "' "
    }

    def stringLinks = ''
    for (int i = 0; i < links.size(); i++) {
        stringLinks += "--link '" + links.get(i) + "' "
    }

    def stringPorts = ''
    for (int i = 0; i < ports.size(); i++) {
        stringPorts += "-p '" + ports.get(i) + "' "
    }

    def stringVolumes = ''
    for (int i = 0; i < volumes.size(); i++) {
        stringVolumes += "-v '" + volumes.get(i) + "' "
    }

    def removeImages = ''
    if (remove) {
        removeImages = '--rm '
    }

    def stringPrivileged = ''
    if (privileged) {
        stringPrivileged='--privileged '
    }

    def script = """
        docker run ${containerName}${stringPrivileged}${stringNetwork}${removeImages}${stringEnv}${stringLinks}${stringPorts}${stringVolumes}${image} ${command}
    """

    print "Going to run:\n$script"

    sh script
}

