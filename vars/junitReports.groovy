/**
 * Makes sure that the reports are in the correct folder for processing them
 * with junit. This will take care that the folder is in the workspace, and
 * will clean it up after.
 *
 * This must be called from the workspace.
 */
def call(reportsFolder, code) {
    def reportsLocation = getGuid("reports-")
    def currentFolder = pwd()

    try {
        echo "JUnit code block."
        code()

    } finally {
        echo "Running JUnit archival"
        sh "mv '${reportsFolder}' '${currentFolder}/${reportsLocation}'"
        junit "${reportsLocation}/*.xml"
        sh "rm -fr ./${reportsLocation}"
    }
}

