package hmda.parser.filing.lar

import cats.implicits._
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.{ Applicant, Ethnicity, Race, Sex }
import hmda.parser.LarParserValidationResult
import hmda.parser.filing.lar.LarParserErrorModel._

sealed trait ApplicantFormatValidator extends LarParser {

  def validateApplicant(
    ethnicity1: String,
    ethnicity2: String,
    ethnicity3: String,
    ethnicity4: String,
    ethnicity5: String,
    otherHispanicOrLatino: String,
    ethnicityObserved: String,
    race1: String,
    race2: String,
    race3: String,
    race4: String,
    race5: String,
    otherNative: String,
    otherAsian: String,
    otherPacific: String,
    raceObserved: String,
    sex: String,
    sexObserved: String,
    age: String,
    creditScore: String,
    creditScoreModel: String,
    otherCreditScore: String,
    coApp: Boolean
  ): LarParserValidationResult[Applicant] = {
    val ageEnum = 
      if (coApp) InvalidCoApplicantAge(age)
      else InvalidApplicantAge(age)
    val credEnum =
      if (coApp) InvalidCoApplicantCreditScore(creditScore)
      else InvalidApplicantCreditScore(creditScore)
    val credModelEnum =
      if (coApp) InvalidCoApplicantCreditScoreModel(creditScoreModel)
      else InvalidApplicantCreditScoreModel(creditScoreModel)

    (
      validateEthnicity(
        ethnicity1,
        ethnicity2,
        ethnicity3,
        ethnicity4,
        ethnicity5,
        otherHispanicOrLatino,
        ethnicityObserved,
        coApp = coApp
      ),
      validateRace(race1, race2, race3, race4, race5, otherNative, otherAsian, otherPacific, raceObserved, coApp = coApp),
      validateSex(sex, sexObserved, coApp = coApp),
      validateIntField(age, ageEnum),
      validateIntField(creditScore, credEnum),
      validateLarCode(CreditScoreEnum, creditScoreModel, credModelEnum),
      validateStr(otherCreditScore)
    ).mapN(Applicant.apply)
  }

  private def validateEthnicity(
    ethnicity1: String,
    ethnicity2: String,
    ethnicity3: String,
    ethnicity4: String,
    ethnicity5: String,
    otherHispanicOrLatino: String,
    ethnicityObserved: String,
    coApp: Boolean
  ): LarParserValidationResult[Ethnicity] = {
    val ethEnum1 =
      if (coApp) InvalidCoApplicantEthnicity(1, ethnicity1)
      else InvalidApplicantEthnicity(1, ethnicity1)
    val ethEnum2 =
      if (coApp) InvalidCoApplicantEthnicity(2, ethnicity2)
      else InvalidApplicantEthnicity(2, ethnicity2)
    val ethEnum3 =
      if (coApp) InvalidCoApplicantEthnicity(3, ethnicity3)
      else InvalidApplicantEthnicity(3, ethnicity3)
    val ethEnum4 =
      if (coApp) InvalidCoApplicantEthnicity(4, ethnicity4)
      else InvalidApplicantEthnicity(4, ethnicity4)
    val ethEnum5 =
      if (coApp) InvalidCoApplicantEthnicity(5, ethnicity5)
      else InvalidApplicantEthnicity(5, ethnicity5)
    val ethObsEnum =
      if (coApp) InvalidCoApplicantEthnicityObserved(ethnicityObserved)
      else InvalidApplicantEthnicityObserved(ethnicityObserved)
    (
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity1, ethEnum1),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity2, ethEnum2),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity3, ethEnum3),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity4, ethEnum4),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity5, ethEnum5),
      validateStr(otherHispanicOrLatino),
      validateLarCode(EthnicityObservedEnum, ethnicityObserved, ethObsEnum)
    ).mapN(Ethnicity.apply)
  }

  private def validateRace(
    race1: String,
    race2: String,
    race3: String,
    race4: String,
    race5: String,
    otherNative: String,
    otherAsian: String,
    otherPacific: String,
    raceObserved: String,
    coApp: Boolean
  ): LarParserValidationResult[Race] = {

    val raceEnum1 =
      if (coApp) InvalidCoApplicantRace(1, race1)
      else InvalidApplicantRace(1, race1)
    val raceEnum2 =
      if (coApp) InvalidCoApplicantRace(2, race2)
      else InvalidApplicantRace(2, race2)
    val raceEnum3 =
      if (coApp) InvalidCoApplicantRace(3, race3)
      else InvalidApplicantRace(3, race3)
    val raceEnum4 =
      if (coApp) InvalidCoApplicantRace(4, race4)
      else InvalidApplicantRace(4, race4)
    val raceEnum5 =
      if (coApp) InvalidCoApplicantRace(5, race5)
      else InvalidApplicantRace(5, race5)
    val raceObsEnum =
      if (coApp) InvalidCoApplicantRaceObserved(raceObserved)
      else InvalidApplicantRaceObserved(raceObserved)
    (
      validateLarCodeOrEmptyField(RaceEnum, race1, raceEnum1),
      validateLarCodeOrEmptyField(RaceEnum, race2, raceEnum2),
      validateLarCodeOrEmptyField(RaceEnum, race3, raceEnum3),
      validateLarCodeOrEmptyField(RaceEnum, race4, raceEnum4),
      validateLarCodeOrEmptyField(RaceEnum, race5, raceEnum5),
      validateStr(otherNative),
      validateStr(otherAsian),
      validateStr(otherPacific),
      validateLarCode(RaceObservedEnum, raceObserved, raceObsEnum)
    ).mapN(Race.apply)
  }

  private def validateSex(
    sex: String,
    sexObserved: String,
    coApp: Boolean
  ): LarParserValidationResult[Sex] = {
    val sexEnum =
      if (coApp) InvalidCoApplicantSex(sex) else InvalidApplicantSex(sex)
    val sexObsEnum =
      if (coApp) InvalidCoApplicantSexObserved(sexObserved)
      else InvalidApplicantSexObserved(sexObserved)
    (
      validateLarCode(SexEnum, sex, sexEnum),
      validateLarCode(SexObservedEnum, sexObserved, sexObsEnum)
    ).mapN(Sex.apply)
  }

}

object ApplicantFormatValidator extends ApplicantFormatValidator
