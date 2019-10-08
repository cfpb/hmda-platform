package hmda.api.http.codec.filing

import hmda.model.filing.ts.{ Address, Contact, TransmittalSheet }
import hmda.model.institution.Agency
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

object TsCodec {

  implicit val addressEncoder: Encoder[Address] = deriveEncoder[Address]
  implicit val addressDecoder: Decoder[Address] = deriveDecoder[Address]

  implicit val contactEncoder: Encoder[Contact] = deriveEncoder[Contact]
  implicit val contactDecoder: Decoder[Contact] = deriveDecoder[Contact]

  implicit val agencyEncoder: Encoder[Agency] = new Encoder[Agency] {
    override def apply(a: Agency): Json = Json.obj(
      ("agency", Json.fromInt(a.code))
    )
  }

  implicit val agencyDecoder: Decoder[Agency] = new Decoder[Agency] {
    override def apply(c: HCursor): Result[Agency] =
      for {
        code <- c.downField("agency").as[Int]
      } yield {
        Agency.valueOf(code)
      }
  }

  implicit val tsEncoder: Encoder[TransmittalSheet] =
    new Encoder[TransmittalSheet] {
      override def apply(a: TransmittalSheet): Json = Json.obj(
        ("id", Json.fromInt(a.id)),
        ("institutionName", Json.fromString(a.institutionName)),
        ("year", Json.fromInt(a.year)),
        ("quarter", Json.fromInt(a.quarter)),
        ("contact", a.contact.asJson),
        ("agency", Json.fromInt(a.agency.code)),
        ("totalLines", Json.fromInt(a.totalLines)),
        ("taxId", Json.fromString(a.taxId)),
        ("LEI", Json.fromString(a.LEI))
      )
    }

  implicit val tsDecoder: Decoder[TransmittalSheet] =
    new Decoder[TransmittalSheet] {
      override def apply(c: HCursor): Result[TransmittalSheet] =
        for {
          id              <- c.downField("id").as[Int]
          institutionName <- c.downField("institutionName").as[String]
          year            <- c.downField("year").as[Int]
          quarter         <- c.downField("quarter").as[Int]
          contact         <- c.downField("contact").as[Contact]
          agency          <- c.downField("agency").as[Int]
          totalLines      <- c.downField("totalLines").as[Int]
          taxId           <- c.downField("taxId").as[String]
          lei             <- c.downField("LEI").as[String]
        } yield
          TransmittalSheet(
            id,
            institutionName,
            year,
            quarter,
            contact,
            Agency.valueOf(agency),
            totalLines,
            taxId,
            lei
          )
    }

}
