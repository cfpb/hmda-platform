package hmda.model.institution

import cats.data.NonEmptyList
import hmda.model.filing.Institution.InstitutionFieldMapping
import hmda.util.CSVConsolidator.{listDeDupeToList, listDeDupeToString}
import io.chrisdavenport.cormorant.CSV
import io.circe._
import io.circe.syntax._

object Institution {

  implicit val headers = List("activityYear|lei|agency|institutionType|institutionId2017|taxId|rssd|emailDomains|respondent|parent|assets|otherLenderCode|topHolder|hmdaFiler|quarterlyFiler|quarterlyFilerHasFiledQ1|quarterlyFilerHasFiledQ2|quarterlyFilerHasFiledQ3|notes\n")

  def empty: Institution = Institution(
    activityYear = 2018,
    LEI = "",
    agency = CFPB,
    institutionType = UndeterminedInstitutionType,
    institutionId_2017 = None,
    taxId = None,
    rssd = -1,
    emailDomains = Nil,
    respondent = Respondent.empty,
    parent = Parent.empty,
    assets = -1,
    otherLenderCode = -1,
    topHolder = TopHolder.empty,
    hmdaFiler = false,
    quarterlyFiler = false,
    quarterlyFilerHasFiledQ1 = false,
    quarterlyFilerHasFiledQ2 = false,
    quarterlyFilerHasFiledQ3 = false,
    notes = ""
  )

  implicit val institutionEncoder: Encoder[Institution] =
    (i: Institution) =>
      Json.obj(
        ("activityYear", Json.fromInt(i.activityYear)),
        ("lei", Json.fromString(i.LEI)),
        ("agency", Json.fromInt(i.agency.code)),
        ("institutionType", Json.fromInt(i.institutionType.code)),
        ("institutionId2017", Json.fromString(i.institutionId_2017.getOrElse(""))),
        ("taxId", Json.fromString(i.taxId.getOrElse(""))),
        ("rssd", Json.fromInt(i.rssd)),
        ("emailDomains", listDeDupeToList(i.emailDomains).asJson),
        ("respondent", i.respondent.asJson),
        ("parent", i.parent.asJson),
        ("assets", Json.fromLong(i.assets)),
        ("otherLenderCode", Json.fromInt(i.otherLenderCode)),
        ("topHolder", i.topHolder.asJson),
        ("hmdaFiler", Json.fromBoolean(i.hmdaFiler)),
        ("quarterlyFiler", Json.fromBoolean(i.quarterlyFiler)),
        ("quarterlyFilerHasFiledQ1", Json.fromBoolean(i.quarterlyFilerHasFiledQ1)),
        ("quarterlyFilerHasFiledQ2", Json.fromBoolean(i.quarterlyFilerHasFiledQ2)),
        ("quarterlyFilerHasFiledQ3", Json.fromBoolean(i.quarterlyFilerHasFiledQ3)),
        ("notes", Json.fromString(i.notes))
      )

  implicit val institutionDecoder: Decoder[Institution] =
    (c: HCursor) =>
      for {
        activityYear           <- c.downField("activityYear").as[Int]
        lei                    <- c.downField("lei").as[String]
        agency                 <- c.downField("agency").as[Int]
        institutionType        <- c.downField("institutionType").as[Int]
        maybeInstitutionId2017 <- c.downField("institutionId2017").as[String]
        maybeTaxId             <- c.downField("taxId").as[String]
        rssdId                 <- c.downField("rssd").as[Int]
        emailDomains           <- c.downField("emailDomains").as[List[String]]
        respondent             <- c.downField("respondent").as[Respondent]
        parent                 <- c.downField("parent").as[Parent]
        assets                 <- c.downField("assets").as[Long]
        otherLenderCode        <- c.downField("otherLenderCode").as[Int]
        topHolder              <- c.downField("topHolder").as[TopHolder]
        hmdaFiler              <- c.downField("hmdaFiler").as[Boolean]
        quarterlyFiler         <- c.downField("quarterlyFiler").as[Boolean]
        quarterlyFilerHasFiledQ1 <- c.downField("quarterlyFilerHasFiledQ1").as[Boolean]
        quarterlyFilerHasFiledQ2 <- c.downField("quarterlyFilerHasFiledQ2").as[Boolean]
        quarterlyFilerHasFiledQ3 <- c.downField("quarterlyFilerHasFiledQ3").as[Boolean]
        notes                    <- c.downField("notes").as[Option[String]]
      } yield {
        val institutionId2017 =
          if (maybeInstitutionId2017 == "") None
          else Some(maybeInstitutionId2017)
        val taxId = if (maybeTaxId == "") None else Some(maybeTaxId)

        Institution(
          activityYear,
          lei,
          Agency.valueOf(agency),
          InstitutionType.valueOf(institutionType),
          institutionId2017,
          taxId,
          rssdId,
          emailDomains,
          respondent,
          parent,
          assets,
          otherLenderCode,
          topHolder,
          hmdaFiler,
          quarterlyFiler,
          quarterlyFilerHasFiledQ1,
          quarterlyFilerHasFiledQ2,
          quarterlyFilerHasFiledQ3,
          notes.getOrElse("")
        )
      }
}

case class Institution(
                        activityYear: Int,
                        LEI: String,
                        agency: Agency,
                        institutionType: InstitutionType,
                        institutionId_2017: Option[String],
                        taxId: Option[String],
                        rssd: Int,
                        emailDomains: Seq[String],
                        respondent: Respondent,
                        parent: Parent,
                        assets: Long,
                        otherLenderCode: Int,
                        topHolder: TopHolder,
                        hmdaFiler: Boolean,
                        quarterlyFiler: Boolean,
                        quarterlyFilerHasFiledQ1: Boolean,
                        quarterlyFilerHasFiledQ2: Boolean,
                        quarterlyFilerHasFiledQ3: Boolean,
                        notes: String
                      ) {
  def toCSV: String = {
    //default printer with comma (,) switched to pipe (|)
    val printer = io.chrisdavenport.cormorant.Printer.generic(
      columnSeperator = "|",
      rowSeperator = "\n",
      escape = "\"",
      surround = "\"",
      additionalEscapes = Set("\r")
    )
    val row = CSV.Row(
      NonEmptyList
        .of(
          activityYear.toString,
          LEI,
          agency.code.toString,
          institutionType.code.toString,
          institutionId_2017.getOrElse(""),
          taxId.getOrElse(""),
          rssd.toString,
          listDeDupeToString(emailDomains),
          respondent.name.getOrElse(""),
          respondent.state.getOrElse(""),
          respondent.city.getOrElse(""),
          parent.idRssd.toString,
          parent.name.getOrElse(""),
          assets.toString,
          otherLenderCode.toString,
          topHolder.idRssd.toString,
          topHolder.name.getOrElse(""),
          hmdaFiler.toString,
          quarterlyFiler.toString,
          quarterlyFilerHasFiledQ1.toString,
          quarterlyFilerHasFiledQ2.toString,
          quarterlyFilerHasFiledQ3.toString,
          notes
        )
        .map(CSV.Field)
    )
    printer.print(row)
  }

  def toLoaderPSV: String =
    s"$activityYear|$LEI|${agency.code}|${institutionType.code}|" +
      s"${institutionId_2017.getOrElse("")}|${taxId.getOrElse("")}|$rssd|${emailDomains
        .mkString(",")}|" +
      s"${respondent.name.getOrElse("")}|${respondent.state.getOrElse("")}|${respondent.city
        .getOrElse("")}|" +
      s"${parent.idRssd}|${parent.name.getOrElse("")}|$assets|${otherLenderCode}|" +
      s"${topHolder.idRssd}|${topHolder.name.getOrElse("")}|f|f|" +
      s"f|f|f"

  def valueOf(field: String): String =
    InstitutionFieldMapping
      .mapping(this)
      .getOrElse(field, s"error: field name mismatch for $field")
}