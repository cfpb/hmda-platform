package hmda.api.http.codec.institution

import hmda.model.institution._
import io.circe.syntax._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import RespondentCodec._
import ParentCodec._
import TopHolderCodec._

object InstitutionCodec {

  implicit val institutionEncoder: Encoder[Institution] =
    new Encoder[Institution] {
      override def apply(i: Institution): Json = Json.obj(
        ("activityYear", Json.fromInt(i.activityYear)),
        ("LEI", Json.fromString(i.LEI.getOrElse(""))),
        ("agency", Json.fromString(i.agency.getOrElse(Agency()).code.toString)),
        ("institutionType",
         Json.fromString(
           i.institutionType.getOrElse(InstitutionType()).code.toString)),
        ("institutionId2017",
         Json.fromString(i.institutionId_2017.getOrElse(""))),
        ("taxId", Json.fromString(i.taxId.getOrElse(""))),
        ("rssd", Json.fromString(i.rssd.getOrElse(""))),
        ("emailDomains", i.emailDomains.asJson),
        ("respondent", i.respondent.asJson),
        ("parent", i.parent.asJson),
        ("assets", Json.fromString(i.assets.map(_.toString).getOrElse(""))),
        ("otherLenderCode",
         Json.fromString(i.otherLenderCode.map(_.toString).getOrElse(""))),
        ("topHolder", i.topHolder.asJson),
        ("hmdaFiler", Json.fromBoolean(i.hmdaFiler))
      )
    }

  implicit val institutionDecoder: Decoder[Institution] =
    new Decoder[Institution] {
      override def apply(c: HCursor): Result[Institution] =
        for {
          activityYear <- c.downField("activityYear").as[Int]
          maybeLEI <- c.downField("LEI").as[String]
          maybeAgency <- c.downField("agency").as[String]
          maybeInstitutionType <- c.downField("institutionType").as[String]
          maybeInstitutionId2017 <- c.downField("institutionId2017").as[String]
          maybeTaxId <- c.downField("taxId").as[String]
          maybeRssdId <- c.downField("rssd").as[String]
          emailDomains <- c.downField("emailDomains").as[List[String]]
          respondent <- c.downField("respondent").as[Respondent]
          parent <- c.downField("parent").as[Parent]
          maybeAssets <- c.downField("assets").as[String]
          maybeOtherLenderCode <- c.downField("otherLenderCode").as[String]
          topHolder <- c.downField("topHolder").as[TopHolder]
          hmdaFiler <- c.downField("hmdaFiler").as[Boolean]
        } yield {
          val lei = if (maybeLEI == "") None else Some(maybeLEI)
          val agency =
            if (maybeAgency == "") None
            else Some(Agency.valueOf(maybeAgency.toInt))
          val institutionType =
            if (maybeInstitutionType == "") None
            else Some(InstitutionType.valueOf(maybeInstitutionType.toInt))
          val institutionId2017 =
            if (maybeInstitutionId2017 == "") None
            else Some(maybeInstitutionId2017)
          val taxId = if (maybeTaxId == "") None else Some(maybeTaxId)
          val rssd = if (maybeRssdId == "") None else Some(maybeRssdId)

          val assets = if (maybeAssets == "") None else Some(maybeAssets.toInt)
          val otherLenderCode =
            if (maybeOtherLenderCode == "") None
            else Some(maybeOtherLenderCode.toInt)

          Institution(
            activityYear,
            lei,
            agency,
            institutionType,
            institutionId2017,
            taxId,
            rssd,
            emailDomains,
            respondent,
            parent,
            assets,
            otherLenderCode,
            topHolder,
            hmdaFiler
          )
        }
    }

}
