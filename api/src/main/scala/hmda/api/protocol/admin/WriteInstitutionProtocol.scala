package hmda.api.protocol.admin

import java.util.NoSuchElementException

import hmda.api.protocol.processing.InstitutionProtocol
import hmda.model.institution.Agency._
import hmda.model.institution._
import spray.json._

trait WriteInstitutionProtocol extends InstitutionProtocol {

  implicit object AgencyJsonFormat extends RootJsonFormat[Agency] {

    override def write(obj: Agency): JsValue = JsString(obj.name)

    override def read(json: JsValue): Agency = json match {
      case JsString(CFPB.name) => CFPB
      case JsString(FDIC.name) => FDIC
      case JsString(FRS.name) => FRS
      case JsString(HUD.name) => HUD
      case JsString(NCUA.name) => NCUA
      case JsString(OCC.name) => OCC
      case JsString(UndeterminedAgency.name) => UndeterminedAgency
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object InstitutionTypeJsonFormat extends RootJsonFormat[InstitutionType] {

    override def write(obj: InstitutionType): JsValue = JsString(obj.entryName)

    override def read(json: JsValue): InstitutionType = {
      json match {
        case JsString(name) =>
          try {
            InstitutionType.withName(name)
          } catch {
            case _: NoSuchElementException => throw DeserializationException(
              s"Unable to translate JSON string into valid InstitutionType value: $name"
            )
          }
        case _ => throw DeserializationException("Unable to deserialize")
      }
    }

  }

  implicit object ExternalIdTypeJsonFormat extends RootJsonFormat[ExternalIdType] {

    override def write(obj: ExternalIdType): JsValue =
      JsObject(
        "code" -> JsString(obj.entryName),
        "name" -> JsString(obj.formattedName)
      )

    override def read(json: JsValue): ExternalIdType = json.asJsObject.getFields("code", "name") match {
      case Seq(code, name) =>
        try {
          ExternalIdType.withName(code.convertTo[String])
        } catch {
          case _: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid ExternalIdType value: $code"
          )
        }
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object DepositoryTypeJsonFormat extends RootJsonFormat[DepositoryType] {

    override def write(obj: DepositoryType): JsValue = JsString(obj.entryName)

    override def read(json: JsValue): DepositoryType = json match {
      case JsString(name) =>
        try {
          DepositoryType.withName(name)
        } catch {
          case _: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid DepositoryType value: $name"
          )
        }

      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object InstitutionJsonFormat extends RootJsonFormat[Institution] {

    implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
    implicit val respondentFormat = jsonFormat5(Respondent.apply)
    implicit val parentFormat = jsonFormat5(Parent.apply)
    implicit val topHolderFormat = jsonFormat5(TopHolder.apply)

    override def write(obj: Institution): JsValue = {
      JsObject(
        "id" -> JsString(obj.id),
        "agency" -> obj.agency.toJson,
        "activityYear" -> JsNumber(obj.activityYear),
        "institutionType" -> obj.institutionType.toJson,
        "cra" -> JsBoolean(obj.cra),
        "externalIds" -> JsArray(obj.externalIds.map(e => e.toJson).toVector),
        "emailDomains" -> JsArray(obj.emailDomains.map(e => e.toJson).toVector),
        "respondent" -> obj.respondent.toJson,
        "hmdaFilerFlag" -> JsBoolean(obj.hmdaFilerFlag),
        "parent" -> obj.parent.toJson,
        "assets" -> JsNumber(obj.assets),
        "otherLenderCode" -> JsNumber(obj.otherLenderCode),
        "topHolder" -> obj.topHolder.toJson
      )
    }

    override def read(json: JsValue): Institution = json.asJsObject.getFields(
      "id",
      "agency",
      "activityYear",
      "institutionType",
      "cra",
      "externalIds",
      "emailDomains",
      "respondent",
      "hmdaFilerFlag",
      "parent",
      "assets",
      "otherLenderCode",
      "topHolder"
    ) match {
        case Seq(id, agency, activityYear, institutionType, cra, externalIds, emailDomains, respondent, hmdaFilerFlag, parent, assets, otherLenderCode, topHolder) =>
          Institution(
            id.convertTo[String],
            agency.convertTo[Agency],
            activityYear.convertTo[Int],
            institutionType.convertTo[InstitutionType],
            cra.convertTo[Boolean],
            externalIds.convertTo[Set[ExternalId]],
            emailDomains.convertTo[Set[String]],
            respondent.convertTo[Respondent],
            hmdaFilerFlag.convertTo[Boolean],
            parent.convertTo[Parent],
            assets.convertTo[Int],
            otherLenderCode.convertTo[Int],
            topHolder.convertTo[TopHolder]
          )
      }
  }
}
