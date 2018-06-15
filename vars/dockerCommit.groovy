/**
 * Do a docker commit directly from the Jenkins, so we can
 * run it from outside the container.
 */
def call(config) {
    def containerName = config?.name
    def imageName = config?.image

    if (!containerName) {
        throw new IllegalArgumentException('You need to pase the container name as `name`')
    }

    if (!imageName) {
        throw new IllegalArgumentException('You need to pass the image name as `image`')
    }

    def builder = new ProcessBuilder('docker', 'commit', containerName, imageName)
        .directory(new File(pwd()))

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

    return process.inputStream.text.substring(0, count)
}

