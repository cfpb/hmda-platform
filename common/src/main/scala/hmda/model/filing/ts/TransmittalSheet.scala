package hmda.model.filing.ts

import hmda.model.filing.{HmdaFileRow, PipeDelimited}
import hmda.model.institution.{Agency, CFPB}
import io.circe._
import io.circe.syntax._

case class TransmittalSheet(
                             id: Int = 1,
                             institutionName: String = "",
                             year: Int = 2018,
                             quarter: Int = 4,
                             contact: Contact = Contact(),
                             agency: Agency = CFPB,
                             totalLines: Int = 0,
                             taxId: String = "",
                             LEI: String = ""
                           ) extends PipeDelimited
  with HmdaFileRow {
  override def toCSV: String = {
    s"$id|$institutionName|$year|$quarter|${contact.toCSV}|${agency.code}|$totalLines|$taxId|$LEI"
  }

  override def valueOf(field: String): String = {
    TsFieldMapping
      .mapping(this)
      .getOrElse(field, s"error: field name mismatch for $field")
  }
}

object TransmittalSheet {
  implicit val tsEncoder: Encoder[TransmittalSheet] =
    (a: TransmittalSheet) =>
      Json.obj(
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

  implicit val tsDecoder: Decoder[TransmittalSheet] =
    (c: HCursor) =>
      for {
        id <- c.downField("id").as[Int]
        institutionName <- c.downField("institutionName").as[String]
        year <- c.downField("year").as[Int]
        quarter <- c.downField("quarter").as[Int]
        contact <- c.downField("contact").as[Contact]
        agency <- c.downField("agency").as[Int]
        totalLines <- c.downField("totalLines").as[Int]
        taxId <- c.downField("taxId").as[String]
        lei <- c.downField("LEI").as[String]
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