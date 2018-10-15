/**
# runContainers

Allows running one or more docker containers.

config:
    stage: Optional[str]  # should wrap it into a stage
    tools: Dict[str, [ToolDict|Callable]]  # config, or the inside block

ToolDict:
    inside: Callable
    docker_params: str
    when: boolean

Samples:

```groovy
runContainers stage: "Type Check",
    tools: [
        "mypy": [
            when: RUN_MYPY_CHECKS,
            inside: {
                sh "cd /src; export MYPYPATH=/src/stubs; mypy ."
            }
        ],

        "flake8": [
            when: RUN_FLAKE8_CHECKS,
            inside: {
                sh "cd /src; flake8 ."
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

    if (config.stage) {
        stage(config.stage, runContainersClosure)
    } else {
        runContainersClosure()
    }
}
