package hmda.model.filing.lar

case class LoanApplicationRegister(
    id: Int = 2,
    LEI: String,
    loan: Loan,
    preapproval: Preapproval,
    actionTakenType: ActionTakenType,
    actionTakenDate: Int,
    geography: Geography,
    applicant: Applicant,
    coApplicant: Applicant,
    purchaserType: PurchaserType,
    rateSpread: String,
    hoepaStatus: HOEPAStatus,
    lienStatus: LienStatus
)
