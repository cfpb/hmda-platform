package hmda.api.http.codec.institution

import hmda.model.institution.Respondent
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object RespondentCodec {

  implicit val respondentEncoder: Encoder[Respondent] =
    new Encoder[Respondent] {
      override def apply(r: Respondent): Json = Json.obj(
        ("name", Json.fromString(r.name.getOrElse(""))),
        ("state", Json.fromString(r.state.getOrElse(""))),
        ("city", Json.fromString(r.city.getOrElse("")))
      )
    }

  implicit val respondentDecoder: Decoder[Respondent] =
    new Decoder[Respondent] {
      override def apply(c: HCursor): Result[Respondent] =
        for {
          maybeName <- c.downField("name").as[String]
          maybeState <- c.downField("state").as[String]
          maybeCity <- c.downField("city").as[String]
        } yield {
          val name = if (maybeName == "") None else Some(maybeName)
          val state = if (maybeState == "") None else Some(maybeState)
          val city = if (maybeCity == "") None else Some(maybeCity)
          Respondent(name, state, city)
        }
    }

}
