def call(String version) {
    if (version == null) {
        version = env.BRANCH_HAME
    }

    return version.matches("^\\d+\\.\\d+.*")
}
