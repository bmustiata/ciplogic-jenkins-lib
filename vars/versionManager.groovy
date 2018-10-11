
def call(extraCommand) {
    def suffix = getGuid()
    def VERSION_MANAGER_IMAGE_NAME = 'version_manager'

    extraCommand = extraCommand ?: ''

    try {
        sh """
            mkdir -p /tmp/version-manager/
            docker cp ${VERSION_MANAGER_IMAGE_NAME}:/src/dist/version-manager /tmp/version-manager/version-manager
            chmod +x /tmp/version-manager/version-manager
            PATH="/tmp/version-manager:\$PATH"
            version-manager ${extraCommand}
        """
    } finally {
        sh "rm -fr /tmp/version-manager || true"
    }
}

