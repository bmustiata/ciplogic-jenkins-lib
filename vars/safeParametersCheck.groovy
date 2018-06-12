def call(self) {
    def safeParameters = self.binding.getVariable("__safeParameters")

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

