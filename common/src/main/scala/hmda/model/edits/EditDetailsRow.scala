package hmda.model.edits

import io.circe._
import io.circe.syntax._

case class EditDetailsRow(id: String, fields: Seq[FieldDetails] = Nil)

object EditDetailsRow {
  implicit val encoder: Encoder[EditDetailsRow] =
    (a: EditDetailsRow) =>
      Json.obj(
        ("id", Json.fromString(a.id)),
        ("fields", a.fields.asJson)
      )

  implicit val editDetailsRowDecoder: Decoder[EditDetailsRow] =
    (c: HCursor) =>
      for {
        id <- c.downField("id").as[String]
        fields <- c.downField("fields").as[Seq[FieldDetails]]
      } yield EditDetailsRow(id, fields)

}