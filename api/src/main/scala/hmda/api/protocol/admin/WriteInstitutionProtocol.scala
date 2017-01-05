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
            case e: NoSuchElementException => throw DeserializationException(
              s"Unable to translate JSON string into valid InstitutionType value: $name"
            )
          }
        case _ => throw DeserializationException("Unable to deserialize")
      }
    }

  }

  implicit object ExternalIdTypeJsonFormat extends RootJsonFormat[ExternalIdType] {

    override def write(obj: ExternalIdType): JsValue = JsString(obj.entryName)

    override def read(json: JsValue): ExternalIdType = json match {
      case JsString(name) =>
        try {
          ExternalIdType.withName(name)
        } catch {
          case e: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid ExternalIdType value: $name"
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
          case e: NoSuchElementException => throw DeserializationException(
            s"Unable to translate JSON string into valid DepositoryType value: $name"
          )
        }

      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object InstitutionJsonFormat extends RootJsonFormat[Institution] {

    override def write(obj: Institution): JsValue = {
      JsObject(
        "id" -> JsString(obj.id),
        "agency" -> obj.agency.toJson,
        "activityYear" -> JsNumber(obj.activityYear),
        "respondentId" -> obj.respondentId.toJson,
        "institutionType" -> obj.institutionType.toJson,
        "cra" -> JsBoolean(obj.cra),
        "externalIds" -> JsArray(obj.externalIds.map(e => e.toJson).toVector),
        "emailDomain2015" -> JsString(obj.emailDomain2015),
        "emailDomain2014" -> JsString(obj.emailDomain2014),
        "emailDomain2013" -> JsString(obj.emailDomain2013),
        "respondentName" -> JsString(obj.respondentName),
        "respondentState" -> JsString(obj.respondentState),
        "respondentCity" -> JsString(obj.respondentCity),
        "respondentFipsStateNumber" -> JsString(obj.respondentFipsStateNumber),
        "hmdaFilerFlag" -> JsBoolean(obj.hmdaFilerFlag),
        "parentRespondentId" -> JsString(obj.parentRespondentId),
        "parentIdRssd" -> JsNumber(obj.parentIdRssd),
        "parentName" -> JsString(obj.parentName),
        "parentCity" -> JsString(obj.parentCity),
        "parentState" -> JsString(obj.parentState),
        "assets" -> JsNumber(obj.assets),
        "otherLenderCode" -> JsNumber(obj.otherLenderCode),
        "topHolderIdRssd" -> JsNumber(obj.topHolderIdRssd),
        "topHolderName" -> JsString(obj.topHolderName),
        "topHolderCity" -> JsString(obj.topHolderCity),
        "topHolderState" -> JsString(obj.topHolderState),
        "topHolderCountry" -> JsString(obj.topHolderCountry)
      )
    }

    override def read(json: JsValue): Institution = json.asJsObject.getFields(
      "id",
      "agency",
      "activityYear",
      "respondentId",
      "institutionType",
      "cra",
      "externalIds",
      "emailDomain2015",
      "emailDomain2014",
      "emailDomain2013",
      "respondentName",
      "respondentState",
      "respondentCity",
      "respondentFipsStateNumber",
      "hmdaFilerFlag",
      "parentRespondentId",
      "parentIdRssd",
      "parentName",
      "parentCity",
      "parentState",
      "assets",
      "otherLenderCode",
      "topHolderIdRssd",
      "topHolderName",
      "topHolderCity",
      "topHolderState",
      "topHolderCountry"
    ) match {
        case Seq(id, agency, activityYear, respondentId, institutionType, cra, externalIds, emailDomain2015,
          emailDomain2014, emailDomain2013, respondentName, respondentState, respondentCity, respondentFipsStateNumber,
          hmdaFilerFlag, parentRespondentId, parentIdRssd, parentName, parentCity, parentState, assets,
          otherLenderCode, topHolderIdRssd, topHolderName, topHolderCity, topHolderState, topHolderCountry) =>
          Institution(
            id.convertTo[String],
            agency.convertTo[Agency],
            activityYear.convertTo[Int],
            respondentId.convertTo[ExternalId],
            institutionType.convertTo[InstitutionType],
            cra.convertTo[Boolean],
            externalIds.convertTo[Set[ExternalId]],
            emailDomain2015.convertTo[String],
            emailDomain2014.convertTo[String],
            emailDomain2013.convertTo[String],
            respondentName.convertTo[String],
            respondentState.convertTo[String],
            respondentCity.convertTo[String],
            respondentFipsStateNumber.convertTo[String],
            hmdaFilerFlag.convertTo[Boolean],
            parentRespondentId.convertTo[String],
            parentIdRssd.convertTo[Int],
            parentName.convertTo[String],
            parentCity.convertTo[String],
            parentState.convertTo[String],
            assets.convertTo[Int],
            otherLenderCode.convertTo[Int],
            topHolderIdRssd.convertTo[Int],
            topHolderName.convertTo[String],
            topHolderCity.convertTo[String],
            topHolderState.convertTo[String],
            topHolderCountry.convertTo[String]
          )
      }

  }

  implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
}
