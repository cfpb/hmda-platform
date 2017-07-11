package hmda.publication.reports

import hmda.model.census.Census
import hmda.model.publication.reports._
import org.scalacheck.Gen

object ReportGenerators {

  implicit def reportTypeGen: Gen[ReportTypeEnum] = {
    Gen.oneOf(ReportTypeEnum.values)
  }

  implicit def actionTakenTypeEnumGen: Gen[ActionTakenTypeEnum] = {
    Gen.oneOf(ActionTakenTypeEnum.values)
  }

  implicit def raceEnumGen: Gen[RaceEnum] = {
    Gen.oneOf(RaceEnum.values)
  }

  implicit def ethnicityEnumGen: Gen[EthnicityEnum] = {
    Gen.oneOf(EthnicityEnum.values)
  }

  implicit def minorityStatusEnumGen: Gen[MinorityStatusEnum] = {
    Gen.oneOf(MinorityStatusEnum.values)
  }

  implicit def characteristicEnumGen: Gen[CharacteristicEnum] = {
    Gen.oneOf(CharacteristicEnum.values)
  }

  implicit def applicantIncomeEnumGen: Gen[ApplicantIncomeEnum] = {
    Gen.oneOf(ApplicantIncomeEnum.values)
  }

  implicit def msaReportGen: Gen[MSAReport] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      state <- Gen.oneOf(Census.states.keys.toSeq)
    } yield MSAReport(id, name, state, Census.states.getOrElse(state, "Alabama"))
  }

  implicit def dispositionGen: Gen[Disposition] = {
    for {
      actionTakenType <- Gen.oneOf(ActionTakenTypeEnum.values)
      count <- Gen.choose(0, Int.MaxValue)
      value <- Gen.choose(0, Int.MaxValue)
    } yield Disposition(actionTakenType, count, value)
  }

  implicit def totalDispositionGen: Gen[List[Disposition]] = {
    ActionTakenTypeEnum.values.map { actionTakenType =>
      for {
        count <- Gen.choose(0, Int.MaxValue)
        value <- Gen.choose(0, Int.MaxValue)
      } yield Disposition(actionTakenType, count, value)
    }.map(g => g.sample.getOrElse(Disposition(ActionTakenTypeEnum.LoansOriginated, 0, 0))).toList
  }

  implicit def raceCharacteristicGen: Gen[RaceCharacteristic] = {
    for {
      race <- raceEnumGen
      dispositions <- Gen.listOf(dispositionGen)
    } yield RaceCharacteristic(race, dispositions)
  }

  implicit def ethnicityCharacteristicGen: Gen[EthnicityCharacteristic] = {
    for {
      ethnicity <- ethnicityEnumGen
      dispositions <- Gen.listOf(dispositionGen)
    } yield EthnicityCharacteristic(ethnicity, dispositions)
  }

  implicit def minorityCharacteristicGen: Gen[MinorityStatusCharacteristic] = {
    for {
      minorityStatus <- minorityStatusEnumGen
      dispositions <- Gen.listOf(dispositionGen)
    } yield MinorityStatusCharacteristic(minorityStatus, dispositions)
  }

  implicit def characteristicGen: Gen[Characteristic] = {
    for {
      characteristic <- Gen.oneOf(raceCharacteristicGen, ethnicityCharacteristicGen, minorityCharacteristicGen)
    } yield characteristic
  }

  implicit def applicantIncomeGen: Gen[ApplicantIncome] = {
    for {
      applicantIncome <- applicantIncomeEnumGen
      borrowerCharacteristic <- Gen.listOf(characteristicGen)
    } yield ApplicantIncome(applicantIncome, borrowerCharacteristic)
  }

}
