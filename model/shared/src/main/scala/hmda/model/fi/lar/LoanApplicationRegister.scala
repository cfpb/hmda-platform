package hmda.model.fi.lar

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class LoanApplicationRegister(
    id: Int,
    respondentId: String,
    agencyCode: Int,
    loan: Loan,
    preapprovals: Int,
    actionTakenType: Int,
    actionTakenDate: Int,
    geography: Geography,
    applicant: Applicant,
    purchaserType: Int,
    denial: Denial,
    rateSpread: String,
    hoepaStatus: Int,
    lienStatus: Int
) {
  def toCSV: String = {
    s"$id|$respondentId|$agencyCode|${loan.id}|${loan.applicationDate}" +
      s"|${loan.loanType}|${loan.propertyType}|${loan.purpose}|${loan.occupancy}" +
      s"|${loan.amount}|$preapprovals|$actionTaken|$actionTakenDate" +
      s"|${geography.msa}|${geography.state}|${geography.county}|${geography.tract}" +
      s"|${applicant.ethnicity}|${applicant.coEthnicity}|${applicant.race1}" +
      s"|${applicant.race2}|${applicant.race3}|${applicant.race4}|${applicant.race5}" +
      s"|${applicant.coRace1}|${applicant.coRace2}|${applicant.coRace3}" +
      s"|${applicant.coRace4}|${applicant.coRace5}|${applicant.sex}|${applicant.coSex}" +
      s"|${applicant.income}|$purchaserType|${denial.reason1}|${denial.reason2}" +
      s"|${denial.reason3}|$rateSpread|$hoepaStatus|$lienStatus"
  }

  def toDAT: String = {
    //TODO: implement DAT output
    ""
  }
}

