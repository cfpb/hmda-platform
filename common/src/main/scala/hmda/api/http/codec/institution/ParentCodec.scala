package hmda.api.http.codec.institution

import hmda.model.institution.Parent
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object ParentCodec {

  implicit val parentEncoder: Encoder[Parent] =
    new Encoder[Parent] {
      override def apply(r: Parent): Json = Json.obj(
        ("idRssd", Json.fromString(r.idRssd.map(_.toString).getOrElse(""))),
        ("name", Json.fromString(r.name.getOrElse("")))
      )
    }

  implicit val parentDecoder: Decoder[Parent] =
    new Decoder[Parent] {
      override def apply(c: HCursor): Result[Parent] =
        for {
          maybeIdRssd <- c.downField("idRssd").as[String]
          maybeName <- c.downField("name").as[String]
        } yield {
          val idRssd = if (maybeIdRssd == "") None else Some(maybeIdRssd)
          val name = if (maybeName == "") None else Some(maybeName)
          Parent(idRssd.map(_.toInt), name)
        }
    }

}
