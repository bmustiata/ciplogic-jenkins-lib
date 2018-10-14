def call(config) {
    def stageName = config.stage ?: "Tool Check"

    if (!config.tools) {
        throw new IllegalArgumentException('You need to pass some containers to run')
    }

    stage(stageName) {
        def parallelContainers = [:]

        config.tools.each { toolName, toolConfig ->
            def shouldRun = !toolConfig.containsKey("when") || toolConfig["when"]

            if (shouldRun) {
                parallelContainers[toolName] = {
                    node {
                        deleteDir()
                        checkout scm

                        def dockerParams = "-v ${pwd()}:/src"
                        dockerParams = toolConfig.docker_params ?
                            "${dockerParams} ${toolConfig.docker_params}" :
                            dockerParams

                        docker.image("germaniumhq/tools-${toolName}")
                              .inside(dockerParams, toolConfig.inside)
                    }
                }
            }
        }

        parallel(parallelContainers)
    }
}
