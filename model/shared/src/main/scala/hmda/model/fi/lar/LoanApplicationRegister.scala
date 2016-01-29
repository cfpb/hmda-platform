package hmda.model.fi.lar

case class LoanApplicationRegister(
  id: Int,
  respondentId: String,
  agencyCode: Int,
  loan: Loan,
  preapprovals: Int,
  actionTaken: Int,
  actionTakenDate: Int,
  geography: Geography,
  applicant: Applicant,
  purchaserType: Int,
  denial: Denial,
  rateSpread: String,
  hoepaStatus: Int,
  lienStatus: Int
)

