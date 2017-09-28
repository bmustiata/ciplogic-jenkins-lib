/**
 * Get a randomly available open port.
 */
def call() {
  def port = -1

  try {
    new ServerSocket(0).withCloseable({s ->
        port = s.getLocalPort()
        s.setReuseAddress(true)
    })
  } catch (Exception e) {
    // ignore on purpose
  }

  port
}

