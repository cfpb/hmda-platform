package hmda.auth

case class VerifiedToken(token: String,
                         id: String,
                         name: String,
                         username: String,
                         email: String,
                         roles: Seq[String])
