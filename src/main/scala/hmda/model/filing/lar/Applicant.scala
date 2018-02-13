package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
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
) extends PipeDelimited {
  override def toCSV: String = {
    s"${ethnicity.ethnicity1.code}|${ethnicity.ethnicity2.code}|${ethnicity.ethnicity3.code}|${ethnicity.ethnicity4.code}|" +
      s"${ethnicity.ethnicity5.code}|$otherHispanicOrLatino|${ethnicityObserved.code}|${race.race1.code}|${race.race2.code}|" +
      s"${race.race3.code}|${race.race4.code}|${race.race5.code}|${raceObserved.code}|$otherNativeRace|$otherAsianRace|" +
      s"$otherPacificIslanderRace|${sex.code}|${coSex.code}|${sexObserved.code}|${coSexObserved.code}|$age|$coAge|" +
      s"$income|$creditScore|${creditScoreType.code}|$otherCreditScoreModel"
  }
}
