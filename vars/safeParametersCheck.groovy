def call(storage, binding) {
    def safeParameters = storage.get("__safeParameters")

    safeParameters.each {
        if (!binding.hasVariable(it.arguments.name)) {
            print("using default var")
            binding.setVariable(it.arguments.name, it.arguments.defaultValue)
            return
        }

        // convert the booleans correctly
        def value = binding.getVariable(it.arguments.name)

        print("symbol: ${it.symbol}")

        if (it.symbol == 'boolean' && !(value instanceof Boolean)) {
            binding.setVariable(it.arguments.name, Boolean.valueOf(value))
        }
    }
}

