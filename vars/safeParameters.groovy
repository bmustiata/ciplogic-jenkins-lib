def call(self, parameterList) {
    self.binding.putVariable("__safeParameters", parameterList)

    return parameters(parameterList)
}

