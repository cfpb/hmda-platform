package hmda.model.institution

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object TopHolder {
  def empty: TopHolder = TopHolder(-1, None)

  implicit val topHolderEncoder: Encoder[TopHolder] =
    new Encoder[TopHolder] {
      override def apply(r: TopHolder): Json = Json.obj(
        ("idRssd", Json.fromInt(r.idRssd)),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val topHolderDecoder: Decoder[TopHolder] =
    new Decoder[TopHolder] {
      override def apply(c: HCursor): Result[TopHolder] =
        for {
          idRssd <- c.downField("idRssd").as[Int]
          maybeName <- c.downField("name").as[String]
        } yield {
          val name = if (maybeName == "") None else Some(maybeName)
          TopHolder(idRssd, name)
        }
    }

}

case class TopHolder(
  idRssd: Int,
  name: Option[String]
) {
  def isEmpty: Boolean =
    this match {
      case TopHolder(-1, None) => true
      case _                   => false
    }
}
