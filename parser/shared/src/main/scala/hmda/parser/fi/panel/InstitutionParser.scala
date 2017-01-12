package hmda.parser.fi.panel

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.{ Depository, NonDepository, UndeterminedDepositoryType }
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import hmda.model.institution._

object InstitutionParser {
  def apply(s: String): Institution = {
    val values = (s + " ").split('|').map(_.trim)
    val agency = convertStringToAgency(values(2))
    val institutionType = convertStringToInstitutionType(values(3))
    val respondentId = convertStringToExternalId(values(1), institutionType, agency)
    Institution(
      values(1),
      agency,
      values(0).toInt,
      institutionType,
      cra = stringToBoolean(values(4)),
      getExternalIdSet(values),
      Set(values(10), values(11), values(12)),
      Respondent(respondentId, values(13), values(14), values(15), values(16)),
      hmdaFilerFlag = stringToBoolean(values(17)),
      Parent(values(18), values(19).toInt, values(20), values(21), values(22)),
      values(23).toInt,
      values(24).toInt,
      TopHolder(values(25).toInt, values(26), values(27), values(28), values(29))
    )
  }

  private def stringToBoolean(s: String): Boolean = {
    s match {
      case "1" => true
      case "0" => false
    }
  }

  private def convertStringToAgency(s: String): Agency = {
    s match {
      case "9" => CFPB
      case "3" => FDIC
      case "2" => FRS
      case "7" => HUD
      case "5" => NCUA
      case "1" => OCC
      case _ => UndeterminedAgency
    }
  }

  private def getExternalIdSet(values: Array[String]): Set[ExternalId] = {
    Set(
      ExternalId(values(5), FederalTaxId),
      ExternalId(values(6), RssdId),
      ExternalId(values(7), FdicCertNo),
      ExternalId(values(8), NcuaCharterId),
      ExternalId(values(9), OccCharterId)
    )
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
          case _ => ExternalId(s, UndeterminedExternalId)
        }
      case NonDepository =>
        a match {
          case FRS => ExternalId(s, RssdId)
          case HUD => ExternalId(s, FederalTaxId) //Special cases here, but may not be necessary to fix
          case UndeterminedAgency => ExternalId(s, UndeterminedExternalId)
          case _ => ExternalId(s, FederalTaxId)
        }
      case _ => ExternalId(s, UndeterminedExternalId)
    }
  }

  private def convertStringToInstitutionType(i: String): InstitutionType = {
    if (Set("1", "2", "3", "7").contains(i)) {
      Bank
    } else if (Set("4", "5").contains(i)) {
      SavingsAndLoan
    } else if (i == "6") {
      CreditUnion
    } else if (i == "8") {
      Affiliate
    } else if (i == "14") {
      IndependentMortgageCompany
    } else if (Set("9", "10", "11", "12", "13", "15", "16", "17").contains(i)) {
      MBS
    } else {
      UndeterminedInstitutionType
    }
  }
}
