package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class Applicant(
    ethnicity: Ethnicity = Ethnicity(),
    race: Race = Race(),
    sex: Sex = Sex(),
    age: Int = 0,
    creditScore: Int = 0,
    creditScoreType: CreditScoreEnum = InvalidCreditScoreCode,
    otherCreditScoreModel: String = ""
)
