/**
 * Get a randomly available open port.
 */
def call() {
  def port = -1

  new ServerSocket(0).withCloseable({s ->
      s.setReuseAddress(true)
      port = s.getLocalPort()
  })

  port
}

