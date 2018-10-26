if (!isMasterBranch()) {
    return
}

ensureDockerTooling tools: "git"

stage('Publish Git') {
    node {
        deleteDir()
        checkout scm

        publishGit repo: "git@github.com:bmustiata/ciplogic-jenkins-lib.git"
    }
}
