/**
 * Run a single docker container
 */
@NonCPS
def call(image_name, config) {
    def env  = config?.env?.collect({envEntry -> "-e $envEntry"}).join(' ')
    def ports = config?.ports?.collect({port -> "-p $port"}).join(' ')
    def links = config?.links?.collect({link -> "--link $link"}).join(' ')

    def docker_params = [env, ports, links]
            .findAll({ !!it })
            .join(' ')

    node {
        sh """
        echo docker --rm -t $docker_params
        """
    }
}


