package hmda.util

object LEIValidator { 

    private val alphanumeric = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).toSet

    def isValidLEIFormat(lei: String): Boolean =
        lei.length == 20 && lei.forall(alphanumeric.contains(_))
}
