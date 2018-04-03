
/**
 * Get the first characters from the git hash, to be used
 * in having some unique identifier that can be tracked back
 * to the commit.
 */
def call(config) {
    def count = config ?: 10

    def builder = new ProcessBuilder('git', 'rev-parse', 'HEAD')
        .directory(new File(pwd()))

    builder.environment().putAll(env)

    def process = builder.start()
    def exitCode = process.waitFor();

    if (exitCode != 0) {
        throw new IllegalStateException("""\
            Process execution failed, exit code: ${exitCode}
            STDERR:
            ${process.errorStream.text}
            STDOUT:
            ${process.inputStream.text}
        """.stripIndent())
    }

    return process.inputStream.text.substring(0, config)
}

