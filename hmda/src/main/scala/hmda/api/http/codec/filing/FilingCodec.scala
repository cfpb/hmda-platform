package hmda.api.http.codec.filing

import hmda.model.filing.FilingStatus
import io.circe._
import io.circe.Decoder.Result

object FilingCodec {

  implicit val filingStatusEncoder: Encoder[FilingStatus] =
    new Encoder[FilingStatus] {
      override def apply(a: FilingStatus): Json = Json.obj(
        ("code", Json.fromInt(a.code)),
        ("message", Json.fromString(a.message))
      )
    }

  implicit val filingStatusDecoder: Decoder[FilingStatus] =
    new Decoder[FilingStatus] {
      override def apply(c: HCursor): Result[FilingStatus] =
        for {
          code <- c.downField("code").as[Int]
        } yield {
          FilingStatus.valueOf(code)
        }
    }
}
