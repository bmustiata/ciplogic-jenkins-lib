def call(storage, self) {
    def safeParameters = storage.get("__safeParameters")
    print(self)

    safeParameters.each {
        print("symbol: ${it.symbol}")
        if (!self.hasVariable(it.arguments.name)) {
            print("using default var")
            self.setVariable(it.arguments.name, it.arguments.defaultValue)
            return
        }

        // convert the booleans correctly
        def value = self.getVariable(it.arguments.name)


        if (it.symbol == 'boolean' && !(value instanceof Boolean)) {
            self.setVariable(it.arguments.name, Boolean.valueOf(value))
        }
    }
}

