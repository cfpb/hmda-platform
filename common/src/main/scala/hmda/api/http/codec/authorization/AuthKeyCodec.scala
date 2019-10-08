package hmda.api.http.codec.authorization

import hmda.auth.AuthKey
import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }

object AuthKeyCodec {

  implicit val authKeyEncoder: Encoder[AuthKey] = new Encoder[AuthKey] {
    override def apply(a: AuthKey): Json = Json.obj(
      ("kid", Json.fromString(a.kid)),
      ("kty", Json.fromString(a.kty)),
      ("alg", Json.fromString(a.alg)),
      ("use", Json.fromString(a.use))
    )
  }

  implicit val authKeyDecoder: Decoder[AuthKey] = new Decoder[AuthKey] {
    override def apply(c: HCursor): Result[AuthKey] =
      for {
        kid <- c.downField("kid").as[String]
        kty <- c.downField("kty").as[String]
        alg <- c.downField("alg").as[String]
        use <- c.downField("use").as[String]
      } yield AuthKey(kid, kty, alg, use)
  }
}
