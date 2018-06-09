
def call(extraCommand) {
    def suffix = getGuid()
    def VERSION_MANAGER_IMAGE_NAME = 'version_manager'

    try {
        sh """
            docker cp ${VERSION_MANAGER_IMAGE_NAME}:/src/dist/version-manager /tmp/version-manager_${suffix}
            chmod +x /tmp/version-manager_${suffix}
            /tmp/version-manager_${suffix} ${extraCommand}
        """
    } finally {
        sh "rm /tmp/version-manager_${suffix} || true"
    }
}

