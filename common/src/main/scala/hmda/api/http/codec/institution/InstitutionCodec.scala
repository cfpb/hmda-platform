package hmda.api.http.codec.institution

import hmda.api.http.codec.institution.ParentCodec._
import hmda.api.http.codec.institution.RespondentCodec._
import hmda.api.http.codec.institution.TopHolderCodec._
import hmda.model.institution._
import io.circe.Decoder.Result
import io.circe.syntax._
import io.circe.{ Decoder, Encoder, HCursor, Json }

object InstitutionCodec {

  implicit val institutionEncoder: Encoder[Institution] =
    new Encoder[Institution] {
      override def apply(i: Institution): Json = Json.obj(
        ("activityYear", Json.fromInt(i.activityYear)),
        ("lei", Json.fromString(i.LEI)),
        ("agency", Json.fromInt(i.agency.code)),
        ("institutionType", Json.fromInt(i.institutionType.code)),
        ("institutionId2017", Json.fromString(i.institutionId_2017.getOrElse(""))),
        ("taxId", Json.fromString(i.taxId.getOrElse(""))),
        ("rssd", Json.fromInt(i.rssd)),
        ("emailDomains", i.emailDomains.asJson),
        ("respondent", i.respondent.asJson),
        ("parent", i.parent.asJson),
        ("assets", Json.fromInt(i.assets)),
        ("otherLenderCode", Json.fromInt(i.otherLenderCode)),
        ("topHolder", i.topHolder.asJson),
        ("hmdaFiler", Json.fromBoolean(i.hmdaFiler))
      )
    }

  implicit val institutionDecoder: Decoder[Institution] =
    new Decoder[Institution] {
      override def apply(c: HCursor): Result[Institution] =
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
          assets                 <- c.downField("assets").as[Int]
          otherLenderCode        <- c.downField("otherLenderCode").as[Int]
          topHolder              <- c.downField("topHolder").as[TopHolder]
          hmdaFiler              <- c.downField("hmdaFiler").as[Boolean]
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
            hmdaFiler
          )
        }
    }

}
