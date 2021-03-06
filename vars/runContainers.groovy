/**
# runContainers

Allows running one or more docker containers.

config:
    stage: Optional[str]  # should wrap it into a stage
    tools: Dict[str, [ToolDict|Callable]]  # config, or the inside block

ToolDict:
    outside: Callable[[], OutsideResult]
    docker_params: str|Callable[[OutsideResult], str]
    inside: Callable[[], None]
    when: boolean

Samples:

```groovy
runContainers stage: "Type Check",
    tools: [
        "mypy": [
            when: RUN_MYPY_CHECKS,
            inside: {
                sh "export MYPYPATH=./stubs; mypy ."
            }
        ],

        "flake8": [
            when: RUN_FLAKE8_CHECKS,
            inside: {
                sh "flake8 ."
            }
        ]
    ]
```

or

```groovy
runContainers tools: [
    ansible: {
        sh """
            ansible ${config.params}
        """
    }
]
```
*/
def call(config) {
    if (!config.tools) {
        throw new IllegalArgumentException('You need to pass some containers to run')
    }

    def runContainersClosure = { ->
        def parallelContainers = [:]

        config.tools.each { toolName, toolConfig ->
            if (!(toolConfig instanceof Map)) {
                toolConfig = [inside: toolConfig]
            }

            def shouldRun = !toolConfig.containsKey("when") || toolConfig["when"]

            if (shouldRun) {
                parallelContainers[toolName] = {
                    def toolTag = toolConfig.tag ?: "latest"

                    if (toolConfig.outside) {
                        toolConfig.outside { outsideResult ->
                            def dockerParams = toolConfig.docker_params ?
                                toolConfig.docker_params(outsideResult) :
                                null

                            docker.image("germaniumhq/tools-${toolName}:${toolTag}")
                                  .inside(dockerParams, toolConfig.inside)
                        }
                    } else {
                        def dockerParams = toolConfig.docker_params ?
                            toolConfig.docker_params :
                            null

                        docker.image("germaniumhq/tools-${toolName}:${toolTag}")
                              .inside(dockerParams, toolConfig.inside)
                    }
                }
            }
        }

        if (parallelContainers.size() == 1) {
            parallelContainers.values().first()()
        } else {
            parallel(parallelContainers)
        }
    }

    if (config.stage) {
        stage(config.stage, runContainersClosure)
    } else {
        runContainersClosure()
    }
}
