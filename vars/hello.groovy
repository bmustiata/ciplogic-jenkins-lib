
@NonCPS
def call(params) {
    def values = params.collect { k, v ->
        "${k} -> ${v}"
    }
    print "PARAMS: ${values}"
}

