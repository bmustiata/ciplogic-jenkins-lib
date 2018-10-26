def call(branches, closure) {
    def parallelVariable = [:]

    branches.each { key, value ->
        parallelVariable."${key}" = {
            closure(key, value)
        }
    }

    parallel(parallelVariable)
}
