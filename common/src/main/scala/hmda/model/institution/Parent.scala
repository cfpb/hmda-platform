package hmda.model.institution

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object Parent {
  def empty: Parent = Parent("", "")

  implicit val parentEncoder: Encoder[Parent] =
    new Encoder[Parent] {
      override def apply(r: Parent): Json = Json.obj(
        ("idRssd", Json.fromString("")),
        ("name", Json.fromString(""))
      )
    }

  implicit val parentDecoder: Decoder[Parent] =
    new Decoder[Parent] {
      override def apply(c: HCursor): Result[Parent] =
        for {
          idRssd <- c.downField("idRssd").as[String]
          maybeName <- c.downField("name").as[String]
        } yield {
          val name = if (maybeName == "") None else Some(maybeName)
          Parent("", "")
        }
    }
}

case class Parent(
  idRssd: String,
  name: String
) {
  def isEmpty: Boolean =
    this match {
      case Parent("", "") => true
      case _                => false
    }
}
