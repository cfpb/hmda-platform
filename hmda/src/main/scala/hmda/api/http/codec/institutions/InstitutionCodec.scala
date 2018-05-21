package hmda.api.http.codec.institutions

import hmda.model.institution.{Agency, Institution, InstitutionType, Respondent}
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
      override def apply(c: HCursor): Result[Institution] = ???
    }

}
