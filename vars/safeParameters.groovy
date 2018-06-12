def call(parameterList) {
    binding.setVariable('__safeParameters', parameterList)

    return parameters(parameterList)
}

