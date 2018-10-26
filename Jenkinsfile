if (!isMasterBranch()) {
    return
}

ensureDockerTooling tools: "git"

stage('Publish Git') {
    node {
        deleteDir()
        checkout scm

        publishGit("git@github.com:bmustiata/ciplogic-jenkins-lib.git")
    }
}
