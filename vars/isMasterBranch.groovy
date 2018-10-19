def call(String version) {
    if (version == null) {
        version = env.BRANCH_NAME
    }

    return version == "master"
}
