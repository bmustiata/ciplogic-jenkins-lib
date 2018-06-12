def call(storage, self) {
    def safeParameters = storage.get("__safeParameters")
    print(self)

    safeParameters.each {
        // convert the booleans correctly
        try {
            def value = self.evaluate(it.arguments.name)

            if (it.symbol == 'booleanParam' && !(value instanceof Boolean)) {
                self.binding.setVariable(it.arguments.name, Boolean.valueOf(value))
            }
        } catch (Exception e) {
            self.binding.setVariable(it.arguments.name, it.arguments.defaultValue)
        }
    }
}

