package hmda.api.http.codec.institutions

import hmda.model.institution.TopHolder
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object TopHolderCodec {

  implicit val topHolderEncoder: Encoder[TopHolder] =
    new Encoder[TopHolder] {
      override def apply(r: TopHolder): Json = Json.obj(
        ("idRssd", Json.fromInt(r.idRssd.getOrElse(0))),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val topHolderDecoder: Decoder[TopHolder] =
    new Decoder[TopHolder] {
      override def apply(c: HCursor): Result[TopHolder] =
        for {
          maybeIdRssd <- c.downField("idRssd").as[Int]
          maybeName <- c.downField("name").as[String]
        } yield {
          val idRssd = if (maybeIdRssd == 0) None else Some(maybeIdRssd)
          val name = if (maybeName == "") None else Some(maybeName)
          TopHolder(idRssd, name)
        }
    }

}
