package hmda.auth
import io.circe.{Decoder, Encoder, HCursor, Json}
// $COVERAGE-OFF$
case class AuthKeys(keys: Seq[AuthKey] = Seq.empty)

case class AuthKey(kid: String = "",
                   kty: String = "",
                   alg: String = "",
                   use: String = "")

object AuthKey {
  implicit val authKeyEncoder: Encoder[AuthKey] = (a: AuthKey) =>
    Json.obj(
      ("kid", Json.fromString(a.kid)),
      ("kty", Json.fromString(a.kty)),
      ("alg", Json.fromString(a.alg)),
      ("use", Json.fromString(a.use))
    )

  implicit val authKeyDecoder: Decoder[AuthKey] = (c: HCursor) =>
    for {
      kid <- c.downField("kid").as[String]
      kty <- c.downField("kty").as[String]
      alg <- c.downField("alg").as[String]
      use <- c.downField("use").as[String]
    } yield AuthKey(kid, kty, alg, use)
}
// $COVERAGE-ON$