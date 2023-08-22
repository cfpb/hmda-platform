package hmda.auth

case class VerifiedToken(token: String, id: String, userId: String, name: String, username: String, email: String, roles: Seq[String], lei: String)

object VerifiedToken {
  def apply(): VerifiedToken =
    VerifiedToken("empty-token", "11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222", "token", "dev", "dev@dev.com", Seq.empty, "lei")
}
