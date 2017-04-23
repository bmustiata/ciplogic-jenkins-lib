def call(config) {
    def image = config?.image
    def command = config?.command ?: ""

    def env = config?.env ?: []
    def links = config?.links ?: []
    def ports = config?.ports ?: []
    def volumes = config?.volumes ?: []
    def remove = config?.remove ?: false

    if (!image) {
        print "You need to specify what image to run, with image:..."
        throw new IllegalArgumentException("Missing `image` name")
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

    def script = """
        docker run ${removeImages}${stringEnv}${stringLinks}${stringPorts}${stringVolumes}${image} ${command}
    """

    print "Going to run:\n$script"

    sh script
}

