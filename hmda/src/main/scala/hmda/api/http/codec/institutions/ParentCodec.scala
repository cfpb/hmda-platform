package hmda.api.http.codec.institutions

import hmda.model.institution.Parent
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object ParentCodec {

  implicit val parentEncoder: Encoder[Parent] =
    new Encoder[Parent] {
      override def apply(r: Parent): Json = Json.obj(
        ("idRssd", Json.fromInt(r.idRssd.getOrElse(0))),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val parentDecoder: Decoder[Parent] =
    new Decoder[Parent] {
      override def apply(c: HCursor): Result[Parent] =
        for {
          maybeIdRssd <- c.downField("idRssd").as[Int]
          maybeName <- c.downField("name").as[String]
        } yield {
          val idRssd = if (maybeIdRssd == 0) None else Some(maybeIdRssd)
          val name = if (maybeName == "") None else Some(maybeName)
          Parent(idRssd, name)
        }
    }

}
