def call(self, parameterList) {
    self.binding.setVariable("__safeParameters", parameterList)

    return parameters(parameterList)
}

