package hmda.auth

case class VerifiedToken(token: String, id: String, name: String, username: String, email: String, roles: Seq[String], lei: String)

object VerifiedToken {
  def apply(): VerifiedToken =
    VerifiedToken("empty-token", "af89acfe-c404-4afb-8c8f-2396a3d06c84", "token", "dev", "dev@dev.com", Seq.empty, "lei")
}
