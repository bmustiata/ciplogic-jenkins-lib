/**
 * Get a random GUID
 * @return String
 */
def call(prefix = "") {
    return prefix + UUID.randomUUID().toString()
}

