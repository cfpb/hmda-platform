package hmda.auth

case class AuthRuleChecker(rule: AuthRule, token: VerifiedToken, comparator: String = "")

trait AuthRule {
    def rule(token: VerifiedToken, comparator: String): Boolean
    def rejectMessage: String
}

object LEISpecificOrAdmin extends AuthRule {
    def rule(token: VerifiedToken, comparator: String): Boolean = {
        val lei = comparator
        if (token.roles.contains("hmdaAdmin")) true
        else
            if (token.lei.nonEmpty){
            val leiList = token.lei.split(',')
            leiList.contains(lei.trim())
            } else false
    }
    def rejectMessage = "Your user is not authorized to access this LEI"
}

object AdminOnly extends AuthRule {
    def rule(token: VerifiedToken, comparator: String = ""): Boolean = {  
        token.roles.contains("hmdaAdmin")
    }

    def rejectMessage = "Only HMDA Administrators may access this resource"
}

object BetaOnlyUser extends AuthRule {   
    def rule(token: VerifiedToken, comparator: String): Boolean = {
        val currentNamespace = comparator
        if (token.roles.contains("betaUser")) {
            currentNamespace == "beta"
        } else true
    }

    def rejectMessage = "Your user is only authorized to access the Beta Submission Platform"
}