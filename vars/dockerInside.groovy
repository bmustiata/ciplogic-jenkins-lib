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
    def daemon = config?.daemon ?: false
    def code = config?.code ?: { -> }

    if (!image) {
        print 'You need to specify what image to run, with image:...'
        throw new IllegalArgumentException('Missing `image` name')
    }

    def containerName = ''
    if (name) {
        containerName = "--name ${name} "
    }

    def stringNetwork = ''
    for (String network: networks) {
        stringNetwork = "--network ${network} "
    }

    def stringEnv = ''
    for (int i = 0; i < env.size(); i++) {
        stringEnv += "-e " + env.get(i) + " "
    }

    def stringLinks = ''
    for (int i = 0; i < links.size(); i++) {
        stringLinks += "--link " + links.get(i) + " "
    }

    def stringPorts = ''
    for (int i = 0; i < ports.size(); i++) {
        stringPorts += "-p " + ports.get(i) + " "
    }

    def stringVolumes = ''
    for (int i = 0; i < volumes.size(); i++) {
        stringVolumes += "-v " + volumes.get(i) + " "
    }

    def stringPrivileged = ''
    if (privileged) {
        stringPrivileged='--privileged '
    }

    def script = "${containerName}${stringPrivileged}${stringNetwork}${stringEnv}${stringLinks}${stringPorts}${stringVolumes}".trim()

    print "Going to run docker.image('${image}', '$script').inside(..)"
    docker.image(image).inside(script, code)
}

