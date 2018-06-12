def call(binding) {
    def safeParameters = binding.getVariable("__safeParameters")
    safeParameters.each {
        print it.name + " -> " + it.defaultValue + " : " + it
    }

    throw new IllegalArgumentException("potato")
}

