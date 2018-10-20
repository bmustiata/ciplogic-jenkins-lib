/*
versionManager() needs the version manager tooling enabled.
*/
def call(extraCommand) {
    extraCommand = extraCommand ?: ''
    runContainers tools: [
        "version-manager": {
            sh "version-manager ${extraCommand}"
        }
    ]
}
