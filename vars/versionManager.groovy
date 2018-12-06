/*
versionManager() needs the version manager tooling enabled.
*/
def call(extraCommand) {
    extraCommand = extraCommand ?: ''
    runContainers tools: [
        "version-manager": {
            sh """
                export BRANCH_NAME="${env.BRANCH_NAME}"
                export BUILD_ID="${env.BUILD_ID}"

                version-manager ${extraCommand}
            """
        }
    ]
}
