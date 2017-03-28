/**
 * Build a docker container. This just allows extra setting build arguments,
 * and a bunch of tags on the end container.
 */
def call(config) {
  sh """
    echo ${config.file}
    ls -la
  """
}

