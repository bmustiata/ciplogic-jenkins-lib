/**
 * Ensures the tooling for running the build.
 */
def call(config) {
    def availableTools = [
        "mypy": """\
            FROM germaniumhq/python:3.6
            ENV REFRESHED_AT 2018.10.14-06:56:31
            RUN pip install mypy
        """,

        "ansible": """\
            FROM germaniumhq/python:3.6
            ENV REFRESHED_AT 2018.10.14-06:58:16
            RUN pip install ansible
        """,

        "flake8": """\
            FROM germaniumhq/python:3.6
            ENV REFRESHED_AT 2018.10.14-06:56:31
            RUN pip install flake8
        """,
    ]

    if (!config.tools) {
        throw new IllegalArgumentException("You need to specify the tools you need in `config.tools`")
    }

    stage('Tooling') {
        def parallelToolBuild = [:]

        config.tools.each { toolName ->
            if (! toolName in availableTools) {
                throw new IllegalArgumentException("Unable to find ${toolName} in available tools: ${availableTools.keySet}")
            }

            parallelToolBuild."${toolName}" = {
                node {
                    deleteDir()
                    writeFile file: "Dockerfile", text: availableTools[toolName]
                    docker.build("germaniumhq/tools-${toolName}")
                }
            }
        }

        parallel(parallelToolBuild)
    }
}
