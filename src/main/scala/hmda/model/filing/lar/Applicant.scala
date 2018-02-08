package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class Applicant(
    ethnicity: Ethnicity,
    otherHispanicOrLatino: String,
    ethnicityObserved: EthnicityObservedEnum,
    race: Race,
    raceObserved: RaceObservedEnum,
    otherNativeRace: String,
    otherAsianRace: String,
    otherPacificIslanderRace: String,
    sex: SexEnum,
    coSex: SexEnum,
    sexObserved: SexObservedEnum,
    coSexObserved: SexObservedEnum,
    age: Int,
    coAge: Int,
    income: String,
    creditScore: Int,
    creditScoreType: CreditScoreEnum,
    otherCreditScoreModel: String
)
