def call(branches, closure) {
    def parallelVariable = [:]

    branches.each { branch ->
        parallelVariable."${branch}" = {
            closure(branch)
        }
    }

    parallel(parallelVariable)
}
