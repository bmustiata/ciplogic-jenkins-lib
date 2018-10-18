def call(config) {
    if (!(config instanceof Map)) {
        config = [type: config]
    }

    withCredentials([file(credentialsId: 'PYPIRC_RELEASE_FILE', variable: 'PYPIRC_RELEASE_FILE')]) {
        sh """
            cp ${env.PYPIRC_RELEASE_FILE} /germanium/.pypirc
            chmod 600 /germanium/.pypirc
        """

        if (config.type == "sdist") {
            sh """
                python setup.py sdist upload -r pypitest
                python setup.py sdist upload -r pypimain
            """
        } else if (config.type == "bdist") {
            sh """
                python setup.py bdist_wheel upload -r pypitest
                python setup.py bdist_wheel upload -r pypimain
            """
        } else {
            throw new IllegalArgumentException("Publishing can be only 'sdist' or 'bdist'")
        }
    }
}
