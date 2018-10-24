def call(config) {
    if (!config || !config.gbs || !config.dockerTag) {
        new IllegalArgumentException(
            "You need to pass the gbs and the dockerTag into the gbsBuild(config) object."
        )
    }

    def gbsPath = config.gbs ?: "/Dockerfile"

    if (!gbsPath.contains("Dockerfile")) {
        gbsPath = "${gbsPath}Dockerfile"
    }

    def gbsPrefix = gbsPath.replaceAll("/[^/]*?\$", "/")

    dockerBuild(
        file: ".${gbsPath}",
        build_args: ["GBS_PREFIX=${gbsPrefix}"],
        tags: [config.dockerTag]
    )
}
