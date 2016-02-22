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
    //TODO: implement CSV output
    ""
  }

  def toDAT: String = {
    //TODO: implement DAT output
    ""
  }
}

