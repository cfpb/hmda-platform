package hmda.parser.fi.panel

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.{Depository, NonDepository}
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import hmda.model.institution.{Agency, ExternalId, Institution, InstitutionType}

/**
  * Created by grippinn on 1/3/17.
  */
object PanelCsvParser {
  def apply(s: String): Institution = {
    val values = (s + " ").split('|').map(_.trim)
    val agency = convertStringToAgency(values(1))
    val institutionType = convertIntToInstitutionType(values(4).toInt)
    Institution(
      values(0),
      agency,
      values(2).toInt,
      convertStringToExternalId(values(3), institutionType, agency),
      institutionType,
      values(5).toBoolean,
      getExternalIdSet(values, institutionType, agency),
      values(11),
      values(12),
      values(13),
      values(14),
      values(15),
      values(16),
      values(17),
      values(18).toBoolean,
      values(19),
      values(20).toInt,
      values(21),
      values(22),
      values(23),
      values(24).toInt,
      values(25).toInt,
      values(26).toInt,
      values(27),
      values(28),
      values(29),
      values(30)
    )
  }

  private def convertStringToAgency(s: String): Agency = {
    s match {
      case "9" => CFPB
      case "3" => FDIC
      case "2" => FRS
      case "7" => HUD
      case "5" => NCUA
      case "1" => OCC
    }
  }

  private def getExternalIdSet(values: Array[String], i: InstitutionType, a: Agency): Set[ExternalId] = {
    Set(convertStringToExternalId(values(6), i, a),
      convertStringToExternalId(values(7), i, a),
      convertStringToExternalId(values(8), i, a),
      convertStringToExternalId(values(9), i, a),
      convertStringToExternalId(values(10), i, a))
  }

  private def convertStringToExternalId(s: String, i: InstitutionType, a: Agency): ExternalId = {
    i.depositoryType match {
      case Depository =>
        a match {
          case OCC => ExternalId(s, OccCharterId)
          case NCUA => ExternalId(s, NcuaCharterId)
          case FDIC => ExternalId(s, FdicCertNo)
          case FRS => ExternalId(s, RssdId)
          case CFPB => ExternalId(s, RssdId)
        }
      case NonDepository =>
        a match {
          case FRS => ExternalId(s, RssdId)
          case HUD => ExternalId(s, FederalTaxId) //Special case, may not be necessary to fix
          case _ => ExternalId(s, FederalTaxId)
        }
    }
  }

  private def convertIntToInstitutionType(i: Int): InstitutionType = {
    if(Set(1, 2, 3, 7).contains(i)) {
      Bank
    }
    else if(Set(4, 5).contains(i)) {
      SavingsAndLoan
    }
    else if(i == 6) {
      CreditUnion
    }
    else if (i == 8) {
      Affiliate
    }
    else if(i == 14) {
      IndependentMortgageCompany
    }
    else {
      MBS
    }
  }
}
