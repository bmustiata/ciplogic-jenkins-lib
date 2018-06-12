def call(binding) {
    binding["__safeParameters"].each {
        print it.name + " -> " + it.defaultValue + " : " + it
    }
    throw new IllegalArgumentException("potato")
}

