def call(versionManagerParams) {
    deleteDir()
    checkout scm

    versionManager(versionManagerParams)
}
