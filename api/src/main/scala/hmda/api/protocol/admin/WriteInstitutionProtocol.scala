package hmda.api.protocol.admin

import hmda.api.protocol.processing.InstitutionProtocol
import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.{ Depository, NonDepository }
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import hmda.model.institution._
import spray.json._

trait WriteInstitutionProtocol extends InstitutionProtocol {

  implicit object AgencyJsonFormat extends RootJsonFormat[Agency] {

    override def write(obj: Agency): JsValue = obj match {
      case CFPB => JsString(CFPB.name)
      case FDIC => JsString(FDIC.name)
      case FRS => JsString(FRS.name)
      case HUD => JsString(HUD.name)
      case NCUA => JsString(NCUA.name)
      case OCC => JsString(OCC.name)
    }

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

    override def write(obj: InstitutionType): JsValue = obj match {
      case Bank => JsString(Bank.entryName)
      case CreditUnion => JsString(CreditUnion.entryName)
      case SavingsAndLoan => JsString(SavingsAndLoan.entryName)
      case IndependentMortgageCompany => JsString(IndependentMortgageCompany.entryName)
      case MBS => JsString(MBS.entryName)
      case Affiliate => JsString(Affiliate.entryName)
      case NonDepositInstType => JsString(NonDepositInstType.entryName)
      case NoDepositTypeInstType => JsString(NoDepositTypeInstType.entryName)
    }

    override def read(json: JsValue): InstitutionType = json match {
      case JsString(Bank.entryName) => Bank
      case JsString(CreditUnion.entryName) => CreditUnion
      case JsString(SavingsAndLoan.entryName) => SavingsAndLoan
      case JsString(IndependentMortgageCompany.entryName) => IndependentMortgageCompany
      case JsString(MBS.entryName) => MBS
      case JsString(Affiliate.entryName) => Affiliate
      case JsString(NonDepositInstType.entryName) => NonDepositInstType
      case JsString(NoDepositTypeInstType.entryName) => NoDepositTypeInstType
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object ExternalIdTypeJsonFormat extends RootJsonFormat[ExternalIdType] {

    override def write(obj: ExternalIdType): JsValue = obj match {
      case FdicCertNo => JsString(FdicCertNo.entryName)
      case FederalTaxId => JsString(FederalTaxId.entryName)
      case NcuaCharterId => JsString(NcuaCharterId.entryName)
      case OccCharterId => JsString(OccCharterId.entryName)
      case RssdId => JsString(RssdId.entryName)
    }

    override def read(json: JsValue): ExternalIdType = json match {
      case JsString(FdicCertNo.entryName) => FdicCertNo
      case JsString(FederalTaxId.entryName) => FederalTaxId
      case JsString(NcuaCharterId.entryName) => NcuaCharterId
      case JsString(OccCharterId.entryName) => OccCharterId
      case JsString(RssdId.entryName) => RssdId
      case _ => throw DeserializationException("Unable to deserialize")
    }

  }

  implicit object DepositoryTypeJsonFormat extends RootJsonFormat[DepositoryType] {

    override def write(obj: DepositoryType): JsValue = obj match {
      case Depository => JsString("depository")
      case NonDepository => JsString("non-depository")
    }

    override def read(json: JsValue): DepositoryType = json match {
      case JsString("depository") => Depository
      case JsString("non-depository") => NonDepository
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
