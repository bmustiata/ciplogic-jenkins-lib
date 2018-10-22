def call(config) {
    if (!(config instanceof Map)) {
        config = [type: config, server: 'pypitest']
    }

    if (!config.server || (
            config.server != "pypitest" &&
            config.server != "pypimain" &&
            config.server != "nexus")) {
        throw new IllegalArgumentException("You need to pass a server in the config: pypitest, pypimain, or nexus, got: ${config.server}.")
    }

    def credentialsFile = config.server == "nexus" ? "PYPIRC_LOCAL_NEXUS" : "PYPIRC_RELEASE_FILE"

    withCredentials([file(credentialsId: credentialsFile, variable: credentialsFile)]) {
        sh """
            cp ${env[credentialsFile]} /germanium/.pypirc
            chmod 600 /germanium/.pypirc
        """

        if (config.type == "sdist") {
            sh """
                python setup.py sdist upload -r ${config.server}
            """
        } else if (config.type == "bdist") {
            sh """
                python setup.py bdist_wheel upload -r ${config.server}
            """
        } else {
            throw new IllegalArgumentException("Publishing can be only 'sdist' or 'bdist'")
        }
    }
}
