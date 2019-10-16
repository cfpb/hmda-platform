package hmda.model.institution

import io.circe._
import io.circe.syntax._

case class HmdaFiler(lei: String, name: String, period: String)
object HmdaFiler {
  implicit val encoder: Encoder[HmdaFiler] =
    (a: HmdaFiler) =>
      Json.obj(
        ("lei", Json.fromString(a.lei)),
        ("name", Json.fromString(a.name)),
        ("period", Json.fromString(a.period))
      )

  implicit val decoder: Decoder[HmdaFiler] =
    (c: HCursor) =>
      for {
        lei    <- c.downField("lei").as[String]
        name   <- c.downField("name").as[String]
        period <- c.downField("period").as[String]
      } yield {
        HmdaFiler(lei, name, period)
      }
}

case class MsaMd(id: String, name: String)

case class HmdaFilerResponse(institutions: Set[HmdaFiler])
object HmdaFilerResponse {
  implicit val encoder: Encoder[HmdaFilerResponse] =
    (a: HmdaFilerResponse) =>
      Json.obj(
        ("institutions", a.institutions.asJson)
      )

  implicit val decoder: Decoder[HmdaFilerResponse] =
    (c: HCursor) =>
      for {
        hmdaFilers <- c.downField("institutions").as[Set[HmdaFiler]]
      } yield {
        HmdaFilerResponse(hmdaFilers)
      }
}

case class MsaMdResponse(institution: HmdaFiler, msaMds: Set[MsaMd])
