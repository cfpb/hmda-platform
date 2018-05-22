package hmda.api.http.codec.institutions

import hmda.model.institution._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import RespondentCodec._
import ParentCodec._
import TopHolderCodec._
import io.circe.Decoder.Result

object InstitutionCodec {

  implicit val institutionEncoder: Encoder[Institution] =
    new Encoder[Institution] {
      override def apply(i: Institution): Json = Json.obj(
        ("activityYear", Json.fromInt(i.activityYear)),
        ("LEI", Json.fromString(i.LEI.getOrElse(""))),
        ("agency", Json.fromInt(i.agency.getOrElse(Agency()).code)),
        ("institutionType",
         Json.fromInt(i.institutionType.getOrElse(InstitutionType()).code)),
        ("institutionId2017",
         Json.fromString(i.institutionId_2017.getOrElse(""))),
        ("taxId", Json.fromString(i.taxId.getOrElse(""))),
        ("rssd", Json.fromString(i.rssd.getOrElse(""))),
        ("emailDomain", Json.fromString(i.emailDomain.getOrElse(""))),
        ("respondent", i.respondent.asJson),
        ("parent", i.parent.asJson),
        ("assets", Json.fromInt(i.assets.getOrElse(0))),
        ("otherLenderCode", Json.fromInt(i.otherLenderCode.getOrElse(0))),
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
          agency <- c.downField("agency").as[Int]
          institutionType <- c.downField("institutionType").as[Int]
          maybeInstitutionId2017 <- c.downField("institutionId2017").as[String]
          maybeTaxId <- c.downField("taxId").as[String]
          maybeRssdId <- c.downField("rssd").as[String]
          maybeEmailDomain <- c.downField("emailDomain").as[String]
          respondent <- c.downField("respondent").as[Respondent]
          parent <- c.downField("parent").as[Parent]
          assets <- c.downField("assets").as[Int]
          otherLenderCode <- c.downField("otherLenderCode").as[Int]
          topHolder <- c.downField("topHolder").as[TopHolder]
          hmdaFiler <- c.downField("hmdaFiler").as[Boolean]
        } yield {
          val lei = if (maybeLEI == "") None else Some(maybeLEI)
          val institutionId2017 =
            if (maybeInstitutionId2017 == "") None
            else Some(maybeInstitutionId2017)
          val taxId = if (maybeTaxId == "") None else Some(maybeTaxId)
          val rssd = if (maybeRssdId == "") None else Some(maybeRssdId)
          val emailDomain =
            if (maybeEmailDomain == "") None else Some(maybeEmailDomain)

          Institution(
            activityYear,
            lei,
            Some(Agency.valueOf(agency)),
            Some(InstitutionType.valueOf(institutionType)),
            institutionId2017,
            taxId,
            rssd,
            emailDomain,
            respondent,
            parent,
            Some(assets),
            Some(otherLenderCode),
            topHolder,
            hmdaFiler
          )
        }
    }

}
