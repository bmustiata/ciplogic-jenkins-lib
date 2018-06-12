def call(storage, parameterList) {
    storage.put("__safeParameters", parameterList)

    return parameters(parameterList)
}

