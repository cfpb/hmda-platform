package hmda.model.filing.lar

case class LoanApplicationRegister(
    id: Int = 2,
    LEI: String,
    loan: Loan,
    preapproval: Preapproval,
    actionTakenType: ActionTakenType,
    actionTakenDate: Int,
    geography: Geography,
    ethnicity1: Ethnicity,
    ethnicity2: Ethnicity,
    ethnicity3: Ethnicity,
    ethnicity4: Ethnicity,
    ethnicity5: Ethnicity,
    otherHispanicOrLatino: String,
    coEthnicity1: Ethnicity,
    coEthnicity2: Ethnicity,
    coEthnicity3: Ethnicity,
    coEthnicity4: Ethnicity,
    coEthnicity5: Ethnicity,
    otherCoHispanicOrLatino: String,
    ethnicityObserved: EthnicityObserved,
    coEthnicityObserved: EthnicityObserved
)
