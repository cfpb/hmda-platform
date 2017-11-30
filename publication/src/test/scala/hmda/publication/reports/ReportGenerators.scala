package hmda.publication.reports

import hmda.model.census.Census
import hmda.model.publication.reports._
import org.scalacheck.Gen

object ReportGenerators {

  implicit def reportTypeGen: Gen[ReportTypeEnum] = {
    Gen.oneOf(ReportTypeEnum.values)
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

  implicit def valueDispositionGen: Gen[ValueDisposition] = {
    for {
      dispositionName <- Gen.alphaStr
      count <- Gen.choose(0, Int.MaxValue)
      value <- Gen.choose(0, Int.MaxValue)
    } yield ValueDisposition(dispositionName, count, value)
  }

  implicit def raceCharacteristicGen: Gen[RaceCharacteristic] = {
    for {
      race <- raceEnumGen
      dispositions <- Gen.listOfN(6, valueDispositionGen)
    } yield RaceCharacteristic(race, dispositions)
  }

  implicit def ethnicityCharacteristicGen: Gen[EthnicityCharacteristic] = {
    for {
      ethnicity <- ethnicityEnumGen
      dispositions <- Gen.listOfN(6, valueDispositionGen)
    } yield EthnicityCharacteristic(ethnicity, dispositions)
  }

  implicit def minorityCharacteristicGen: Gen[MinorityStatusCharacteristic] = {
    for {
      minorityStatus <- minorityStatusEnumGen
      dispositions <- Gen.listOfN(6, valueDispositionGen)
    } yield MinorityStatusCharacteristic(minorityStatus, dispositions)
  }

  implicit def raceBorrowerCharacteristicGen: Gen[RaceBorrowerCharacteristic] = {
    for {
      raceList <- Gen.listOfN(8, raceCharacteristicGen)
    } yield RaceBorrowerCharacteristic(raceList)
  }

  implicit def ethnicityBorrowerCharacteristic: Gen[EthnicityBorrowerCharacteristic] = {
    for {
      ethnicityList <- Gen.listOfN(4, ethnicityCharacteristicGen)
    } yield EthnicityBorrowerCharacteristic(ethnicityList)
  }

  implicit def minorityStatusBorrowerCharacteristic: Gen[MinorityStatusBorrowerCharacteristic] = {
    for {
      minorityStatusList <- Gen.listOfN(2, minorityCharacteristicGen)
    } yield MinorityStatusBorrowerCharacteristic(minorityStatusList)
  }

  implicit def characteristicGen: Gen[Characteristic] = {
    for {
      characteristic <- Gen.oneOf(raceCharacteristicGen, ethnicityCharacteristicGen, minorityCharacteristicGen)
    } yield characteristic
  }

  implicit def borrowerCharacteristicGen: Gen[BorrowerCharacteristic] = {
    for {
      borrowerCharacteristic <- Gen.oneOf(raceBorrowerCharacteristicGen, ethnicityBorrowerCharacteristic, minorityStatusBorrowerCharacteristic)
    } yield borrowerCharacteristic
  }

  implicit def applicantIncomeGen: Gen[ApplicantIncome] = {
    for {
      applicantIncome <- applicantIncomeEnumGen
      borrowerCharacteristic <- Gen.listOfN(3, borrowerCharacteristicGen)
    } yield ApplicantIncome(applicantIncome, borrowerCharacteristic)
  }

}
