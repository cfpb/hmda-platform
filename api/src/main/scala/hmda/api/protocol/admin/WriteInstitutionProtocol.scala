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
        "name" -> JsString(obj.name),
        "externalIds" -> JsArray(obj.externalIds.map(e => e.toJson).toVector),
        "agency" -> obj.agency.toJson,
        "institutionType" -> obj.institutionType.toJson,
        "hasParent" -> JsBoolean(obj.hasParent),
        "cra" -> JsBoolean(obj.cra),
        "status" -> obj.status.toJson
      )
    }

    override def read(json: JsValue): Institution = json.asJsObject.getFields(
      "id",
      "name",
      "externalIds",
      "agency",
      "institutionType",
      "hasParent",
      "cra",
      "status"
    ) match {
        case Seq(id, name, externalIds, agency, institutionType, hasParent, cra, status) =>
          Institution(
            id.convertTo[String],
            name.convertTo[String],
            externalIds.convertTo[Set[ExternalId]],
            agency.convertTo[Agency],
            institutionType.convertTo[InstitutionType],
            hasParent.convertTo[Boolean],
            cra.convertTo[Boolean],
            status.convertTo[InstitutionStatus]
          )
      }

  }

  implicit val externalIdFormat = jsonFormat2(ExternalId.apply)
}
