package hmda.api.http.codec.institution

import hmda.model.institution.TopHolder
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object TopHolderCodec {

  implicit val topHolderEncoder: Encoder[TopHolder] =
    new Encoder[TopHolder] {
      override def apply(r: TopHolder): Json = Json.obj(
        ("idRssd", Json.fromString(r.idRssd.map(_.toString).getOrElse(""))),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val topHolderDecoder: Decoder[TopHolder] =
    new Decoder[TopHolder] {
      override def apply(c: HCursor): Result[TopHolder] =
        for {
          maybeIdRssd <- c.downField("idRssd").as[String]
          maybeName <- c.downField("name").as[String]
        } yield {
          val idRssd = if (maybeIdRssd == "") None else Some(maybeIdRssd)
          val name = if (maybeName == "") None else Some(maybeName)
          TopHolder(idRssd.map(_.toInt), name)
        }
    }

}
