def call(String ... artifacts) {
    artifacts.each { artifact ->
        def artifactName = artifact.replaceAll("^.*/", "")

        sh """
            mkdir -p '${pwd()}/_archive'
            cp '${artifact}' '${pwd()}/_archive/${artifactName}'
        """

        archiveArtifacts(
            artifacts: "_archive/${artifactName}",
            fingerprint: true
        )
    }
}
