package hmda.model.institution

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object TopHolder {
  def empty: TopHolder = TopHolder("", "")

  implicit val topHolderEncoder: Encoder[TopHolder] =
    new Encoder[TopHolder] {
      override def apply(r: TopHolder): Json = Json.obj(
        ("idRssd", Json.fromString("")),
        ("name", Json.fromString(""))
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
          TopHolder(String, String)
        }
    }

}

case class TopHolder(
  idRssd: String,
  name: String
) {
  def isEmpty: Boolean =
    this match {
      case TopHolder(String, String) => true
      case _                   => false
    }
}
