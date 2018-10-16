package hmda.auth

case class AuthKeys(keys: Seq[AuthKey] = Seq.empty)

case class AuthKey(kid: String = "",
                   kty: String = "",
                   alg: String = "",
                   use: String = "")
