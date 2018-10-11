
def call(extraCommand) {
    def suffix = getGuid()
    def VERSION_MANAGER_IMAGE_NAME = 'version_manager'

    extraCommand = extraCommand ?: ''

    try {
        sh """
            mkdir -p /tmp/version-manager_${suffix}/
            docker cp ${VERSION_MANAGER_IMAGE_NAME}:/src/dist/version-manager /tmp/version-manager_${suffix}/version-manager
            chmod +x /tmp/version-manager_${suffix}/version-manager
            PATH="/tmp/version-manager_${suffix}:\$PATH"
            version-manager ${extraCommand}
        """
    } finally {
        sh "rm -fr /tmp/version-manager_${suffix} || true"
    }
}

