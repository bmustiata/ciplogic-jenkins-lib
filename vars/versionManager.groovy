/*
versionManager() needs the version manager tooling enabled.
*/
def call(extraCommand) {
    extraCommand = extraCommand ?: ''
    runContainers tools: [
        "version-manager": {
            echo "VERSION_MANAGER"
            sh """
                set
            """
            sh "version-manager ${extraCommand}"
        }
    ]
}
