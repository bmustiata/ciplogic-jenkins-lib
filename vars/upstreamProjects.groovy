def call(config) {
    if (!config) {
        throw new IllegalArgumentException("You need to specify the upstream projects")
    }

    if (!(config instanceof Map)) {
        config = [ projects: config ]
    }

    def encodedBranchName = URLEncoder.encode(env.BRANCH_NAME, "utf-8")

    upstream(
        threshold: 'SUCCESS',
        upstreamProjects: "${config.projects}/${encodedBranchName}"
    )
}
