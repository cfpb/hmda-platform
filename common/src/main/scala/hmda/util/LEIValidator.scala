package hmda.util

object LEIValidator { 

    val leiRegex = "([A-Z0-9]{20})"

    val leiKeyRegex = "(?<lei>[A-Z0-9]{20})"

    def isValidLEIFormat(lei: String): Boolean =
        lei.matches(leiRegex)

}
