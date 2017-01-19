package hmda.api.model

import hmda.model.fi.lar.{ Applicant, Denial, Geography, Loan }

case class ModifiedLoanApplicationRegister(
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
)
