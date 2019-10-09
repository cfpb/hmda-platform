package hmda.model.institution

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object Respondent {
  def empty: Respondent =
    Respondent(
      None,
      None,
      None
    )
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

case class Respondent(
  name: Option[String],
  state: Option[String],
  city: Option[String]
) {
  def isEmpty: Boolean =
    this match {
      case Respondent(None, None, None) => true
      case _                            => false
    }
}
