package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class Applicant(
    ethnicity: Ethnicity,
    race: Race,
    sex: Sex,
    age: Int,
    creditScore: Int,
    creditScoreType: CreditScoreEnum,
    otherCreditScoreModel: String
)
