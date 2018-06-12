def call(binding) {
    def safeParameters = binding.getVariable("__safeParameters")
    safeParameters.each {
        print it.symbol + " -> " + it.klass + " : " + it.arguments + " m: " + it.model
    }

    throw new IllegalArgumentException("potato")
}

