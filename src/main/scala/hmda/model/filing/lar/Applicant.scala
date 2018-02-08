package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class Applicant(
    ethnicity1: EthnicityEnum,
    ethnicity2: EthnicityEnum,
    ethnicity3: EthnicityEnum,
    ethnicity4: EthnicityEnum,
    ethnicity5: EthnicityEnum,
    otherHispanicOrLatino: String,
    ethnicityObserved: EthnicityObservedEnum,
    race1: RaceEnum,
    race2: RaceEnum,
    race3: RaceEnum,
    race4: RaceEnum,
    race5: RaceEnum,
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
