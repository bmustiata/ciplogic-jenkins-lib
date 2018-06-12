def call(binding) {
    def safeParameters = binding.getVariable("__safeParameters")

    safeParameters.each {
        if (!binding.hasVariable(it.arguments.name)) {
            binding.setVariable(it.arguments.name, it.arguments.defaultValue)
        }
    }
}

