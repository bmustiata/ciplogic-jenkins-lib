/**
 * Makes sure that the reports are in the correct folder for processing them
 * with junit. This will take care that the folder is in the workspace, and
 * will clean it up after.
 */
def call(reportsFolder, code) {
    def reportsLocation = getGuid("reports-")

    try {
        code()

        sh "mv '${reportsFolder}' '${reportsLocation}'"
    } finally {
        junit "${reportsLocation}/*.xml"
        sh "rm -fr ./${reportsLocation}"
    }
}

