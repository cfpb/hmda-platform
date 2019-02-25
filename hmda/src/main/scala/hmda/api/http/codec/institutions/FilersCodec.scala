package hmda.api.http.codec.institutions

import hmda.model.institution.HmdaFiler
import hmda.model.institution.HmdaFilerResponse
import io.circe._
import io.circe.Decoder.Result
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._
import io.circe.generic.auto._

object FilersCodec {

  implicit val hmdaFilerEncoder: Encoder[HmdaFiler] =
    new Encoder[HmdaFiler] {
      override def apply(a: HmdaFiler): Json = Json.obj(
        ("lei", Json.fromString(a.name)),
        ("name", Json.fromString(a.name)),
        ("period", Json.fromString(a.period))
      )
    }

  implicit val hmdaFilerDecoder: Decoder[HmdaFiler] =
    new Decoder[HmdaFiler] {
      override def apply(c: HCursor): Result[HmdaFiler] =
        for {
          lei <- c.downField("lei").as[String]
          name <- c.downField("name").as[String]
          period <- c.downField("period").as[String]
        } yield {
          HmdaFiler(lei, name, period)
        }
    }

  implicit val HmdaFilerResponseEncoder: Encoder[HmdaFilerResponse] =
    new Encoder[HmdaFilerResponse] {
      override def apply(a: HmdaFilerResponse): Json = Json.obj(
        ("institutions", a.institutions.asJson)
      )
    }

  implicit val HmdaFilerResponseDecoder: Decoder[HmdaFilerResponse] =
    new Decoder[HmdaFilerResponse] {
      override def apply(c: HCursor): Result[HmdaFilerResponse] =
        for {
          hmdaFilers <- c.downField("institutions").as[Set[HmdaFiler]]
        } yield {
          HmdaFilerResponse(hmdaFilers)
        }
    }
}
