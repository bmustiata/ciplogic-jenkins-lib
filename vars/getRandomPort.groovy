/**
 * Get a randomly available open port.
 */
def call() {
  def port = -1

  for (int i = 0; i < 5; i++) {
    try {
      new ServerSocket(0).withCloseable({s ->
          s.setReuseAddress(true)
          port = s.getLocalPort()
      })

      return port
    } catch (Exception e) {
      // ignore on purpose
    }
  }

  port
}

