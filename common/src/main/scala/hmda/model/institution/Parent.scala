package hmda.model.institution

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object Parent {
  def empty: Parent = Parent(-1, None)

  implicit val parentEncoder: Encoder[Parent] =
    new Encoder[Parent] {
      override def apply(r: Parent): Json = Json.obj(
        ("idRssd", Json.fromInt(r.idRssd)),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val parentDecoder: Decoder[Parent] =
    new Decoder[Parent] {
      override def apply(c: HCursor): Result[Parent] =
        for {
          idRssd <- c.downField("idRssd").as[Int]
          maybeName <- c.downField("name").as[String]
        } yield {
          val name = if (maybeName == "") None else Some(maybeName)
          Parent(idRssd, name)
        }
    }
}

case class Parent(
  idRssd: Int,
  name: Option[String]
) {
  def isEmpty: Boolean =
    this match {
      case Parent(-1, None) => true
      case _                => false
    }
}
