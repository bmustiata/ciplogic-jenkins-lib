/*
Usage:
gbs(platform: 'python:3.6',
    dockerTag: '123',
    prefix?: '/') -> returns a docker.image  // must end in /
*/
def createDockerImage(config, dockerfileContent) {
    def dockerFileName = "/tmp/_Dockerfile.gbs-${getGuid()}"
    def dockerFile = new File(dockerFileName)

    try {
        dockerFile.write dockerfileContent.stripIndent()

        return docker.build(config.dockerTag, "--network=host -f ${dockerFileName} .")
    } finally {
        dockerFile.delete()
    }
}

def call() {
    return [
        test: { config ->
            def GBS_PREFIX = config.prefix ?: '/'
            return createDockerImage(config,
                """\
                FROM germaniumhq/${config.platform}

                #======================================
                # Install prerequisite software
                #======================================
                USER root

                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/install-software /src${GBS_PREFIX}_gbs/install-software
                RUN echo "################################################################################" &&\
                    echo "# INSTALL SOFTWARE" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/install-software/install-software.sh &&\
                    chown -R germanium:germanium /src

                #======================================
                # Prepare dependencies for download
                #======================================
                USER germanium

                # build1
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build1 /src${GBS_PREFIX}_gbs/prepare-build1
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 1" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build1/prepare-build1.sh

                # build2
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build2 /src${GBS_PREFIX}_gbs/prepare-build2
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 2" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build2/prepare-build2.sh

                # build3
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build3 /src${GBS_PREFIX}_gbs/prepare-build3
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 3" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build3/prepare-build3.sh

                # test1
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-test1 /src${GBS_PREFIX}_gbs/prepare-test1
                RUN echo "################################################################################" &&\
                    echo "# PREPARE TEST 1" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-test1/prepare-test1.sh

                # test2
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-test2 /src${GBS_PREFIX}_gbs/prepare-test2
                RUN echo "################################################################################" &&\
                    echo "# PREPARE TEST 2" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-test2/prepare-test2.sh

                # sources are copied only after the test stage
                COPY --chown=germanium:germanium . /src
                """
            )
        },

        build: { config ->
            def GBS_PREFIX = config.prefix ?: '/'
            return createDockerImage(config,
                """\
                FROM germaniumhq/${config.platform}

                #======================================
                # Install prerequisite software
                #======================================
                USER root

                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/install-software /src${GBS_PREFIX}_gbs/install-software
                RUN echo "################################################################################" &&\
                    echo "# INSTALL SOFTWARE" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/install-software/install-software.sh &&\
                    chown -R germanium:germanium /src

                #======================================
                # Prepare dependencies for download
                #======================================
                USER germanium

                # build1
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build1 /src${GBS_PREFIX}_gbs/prepare-build1
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 1" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build1/prepare-build1.sh

                # build2
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build2 /src${GBS_PREFIX}_gbs/prepare-build2
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 2" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build2/prepare-build2.sh

                # build3
                COPY --chown=germanium:germanium ${GBS_PREFIX}_gbs/prepare-build3 /src${GBS_PREFIX}_gbs/prepare-build3
                RUN echo "################################################################################" &&\
                    echo "# PREPARE BUILD 3" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/prepare-build3/prepare-build3.sh

                # sources are copied only after the test stage
                COPY --chown=germanium:germanium . /src

                # run the build
                RUN echo "################################################################################" &&\
                    echo "# RUN BUILD" && \
                    echo "################################################################################" &&\
                    cd /src && \
                    /src${GBS_PREFIX}_gbs/run-build.sh
                """
            )
        }

    ]
}
