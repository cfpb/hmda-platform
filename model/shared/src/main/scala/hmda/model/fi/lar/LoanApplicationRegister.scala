package hmda.model.fi.lar

import hmda.model.fi.{ HasControlNumber, HmdaFileRow, StringPaddingUtils }

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class LoanApplicationRegister(
    id: Int = 0,
    respondentId: String = "",
    agencyCode: Int = 0,
    loan: Loan = Loan("", "", 0, 0, 0, 0, 0),
    preapprovals: Int = 0,
    actionTakenType: Int = 0,
    actionTakenDate: Int = 0,
    geography: Geography = Geography("", "", "", ""),
    applicant: Applicant = Applicant(0, 0, 0, "", "", "", "", 0, "", "", "", "", 0, 0, ""),
    purchaserType: Int = 0,
    denial: Denial = Denial("", "", ""),
    rateSpread: String = "",
    hoepaStatus: Int = 0,
    lienStatus: Int = 0
) extends HasControlNumber with HmdaFileRow with StringPaddingUtils {

  override def valueOf(field: String): Any = {
    LarFieldMapping.mapping(this).getOrElse(field, "error: field name mismatch")
  }

  def toCSV: String = {
    s"$id|$respondentId|$agencyCode|${loan.id}|${loan.applicationDate}" +
      s"|${loan.loanType}|${loan.propertyType}|${loan.purpose}|${loan.occupancy}" +
      s"|${loan.amount}|$preapprovals|$actionTakenType|$actionTakenDate" +
      s"|${geography.msa}|${geography.state}|${geography.county}|${geography.tract}" +
      s"|${applicant.ethnicity}|${applicant.coEthnicity}|${applicant.race1}" +
      s"|${applicant.race2}|${applicant.race3}|${applicant.race4}|${applicant.race5}" +
      s"|${applicant.coRace1}|${applicant.coRace2}|${applicant.coRace3}" +
      s"|${applicant.coRace4}|${applicant.coRace5}|${applicant.sex}|${applicant.coSex}" +
      s"|${applicant.income}|$purchaserType|${denial.reason1}|${denial.reason2}" +
      s"|${denial.reason3}|$rateSpread|$hoepaStatus|$lienStatus"
  }

  /**
   * NOTE:  The DAT file format is not supported by CFPB
   */
  def toDAT: String = {
    id +
      padLeftWithZero(respondentId, 10) +
      agencyCode +
      padRight(loan.id, 25) +
      padRight(loan.applicationDate, 8) +
      loan.loanType +
      loan.propertyType +
      loan.purpose +
      loan.occupancy +
      padLeftWithZero(loan.amount.toString, 5) +
      preapprovals +
      actionTakenType +
      actionTakenDate +
      padRight(geography.msa, 5) +
      geography.state +
      padNumOrNa(geography.county, 3) +
      padNumOrNa(geography.tract, 7) +
      applicant.ethnicity +
      applicant.coEthnicity +
      applicant.race1 +
      padRight(applicant.race2, 1) +
      padRight(applicant.race3, 1) +
      padRight(applicant.race4, 1) +
      padRight(applicant.race5, 1) +
      applicant.coRace1 +
      padRight(applicant.coRace2, 1) +
      padRight(applicant.coRace3, 1) +
      padRight(applicant.coRace4, 1) +
      padRight(applicant.coRace5, 1) +
      applicant.sex +
      applicant.coSex +
      padNumOrNa(applicant.income, 4) +
      purchaserType +
      padRight(denial.reason1, 1) +
      padRight(denial.reason2, 1) +
      padRight(denial.reason3, 1) +
      padNumOrNa(rateSpread, 5) +
      hoepaStatus +
      lienStatus +
      (" " * 270)
  }
}
