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

        "python": """\
            FROM germaniumhq/python:3.6
        """,

        "behave": """\
            FROM germaniumhq/python:3.6

            RUN pip install behave
        """,

        "git": """\
            FROM germaniumhq/ubuntu:18.04
            ENV REFRESHED_AT 2018.10.18-05:25:08
            USER root
            RUN apt update -y && apt install -y git && rm -rf /var/lib/apt/lists/*
            USER germanium
        """,

        "version-manager": """\
            FROM bmst/version-manager
        """
    ]

    if (!config.tools) {
        throw new IllegalArgumentException("You need to specify the tools you need in `config.tools`")
    }

    if (!(config.tools instanceof List)) {
        config.tools = [config.tools]
    }

    stage('Tooling') {
        def parallelToolBuild = [:]

        config.tools.each { tool ->
            if (!(tool instanceof Map)) {
                tool = [
                    name: tool
                ]
            }

            if (!availableTools.containsKey(tool.name)) {
                throw new IllegalArgumentException("Unable to find ${tool.name} in available tools: ${availableTools.keySet()}")
            }

            def shouldRun = !tool.containsKey("when") || tool["when"]

            if (shouldRun) {
                parallelToolBuild."${tool.name}" = {
                    node {
                        if (tool.before) {
                            tool.before()
                        } else {
                            deleteDir()
                        }

                        def toolTag = tool.tag ?: "latest"

                        writeFile file: "Dockerfile", text: availableTools[tool.name]
                        docker.build("germaniumhq/tools-${tool.name}:${toolTag}")
                    }
                }
            }
        }

        parallel(parallelToolBuild)
    }
}
