def call(binding, parameterList) {
    binding.setVariable("__safeParameters", parameterList)

    return parameters(parameterList)
}

