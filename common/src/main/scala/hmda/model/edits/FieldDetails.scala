package hmda.model.edits

import io.circe._

case class FieldDetails(name: String, value: String)

object FieldDetails {
  implicit val encoder: Encoder[FieldDetails] =
    (a: FieldDetails) =>
      Json.obj(
        ("name", Json.fromString(a.name)),
        ("value", Json.fromString(a.value))
      )

  implicit val decoder: Decoder[FieldDetails] =
    (c: HCursor) =>
      for {
        name <- c.downField("name").as[String]
        value <- c.downField("value").as[String]
      } yield FieldDetails(name, value)

}