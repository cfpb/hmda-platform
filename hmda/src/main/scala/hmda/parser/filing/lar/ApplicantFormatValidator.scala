package hmda.parser.filing.lar

import hmda.model.filing.lar.{Applicant, Ethnicity, Race, Sex}
import hmda.model.filing.lar.enums._
import hmda.parser.filing.lar.LarParserErrorModel._
import cats.implicits._
import hmda.parser.LarParserValidationResult

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
    val credEnum =
      if (coApp) InvalidCoApplicantCreditScore else InvalidApplicantCreditScore
    val credModelEnum =
      if (coApp) InvalidCoApplicantCreditScoreModel
      else InvalidApplicantCreditScoreModel

    (
      validateEthnicity(ethnicity1,
                        ethnicity2,
                        ethnicity3,
                        ethnicity4,
                        ethnicity5,
                        otherHispanicOrLatino,
                        ethnicityObserved,
                        coApp = coApp),
      validateRace(race1,
                   race2,
                   race3,
                   race4,
                   race5,
                   otherNative,
                   otherAsian,
                   otherPacific,
                   raceObserved,
                   coApp = coApp),
      validateSex(sex, sexObserved, coApp = coApp),
      validateIntField(age, InvalidAge),
      validateIntField(creditScore, credEnum),
      validateLarCode(CreditScoreEnum, creditScoreModel, credModelEnum),
      validateStr(otherCreditScore)
    ).mapN(Applicant)
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
    val ethEnum =
      if (coApp) InvalidCoApplicantEthnicity else InvalidApplicantEthnicity
    val ethObsEnum =
      if (coApp) InvalidCoApplicantEthnicityObserved
      else InvalidApplicantEthnicityObserved
    (
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity1, ethEnum),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity2, ethEnum),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity3, ethEnum),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity4, ethEnum),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity5, ethEnum),
      validateStr(otherHispanicOrLatino),
      validateLarCode(EthnicityObservedEnum, ethnicityObserved, ethObsEnum)
    ).mapN(Ethnicity)
  }

  private def validateRace(race1: String,
                           race2: String,
                           race3: String,
                           race4: String,
                           race5: String,
                           otherNative: String,
                           otherAsian: String,
                           otherPacific: String,
                           raceObserved: String,
                           coApp: Boolean): LarParserValidationResult[Race] = {

    val raceEnum = if (coApp) InvalidCoApplicantRace else InvalidApplicantRace
    val raceObsEnum =
      if (coApp) InvalidCoApplicantRaceObserved
      else InvalidApplicantRaceObserved
    (
      validateLarCodeOrEmptyField(RaceEnum, race1, raceEnum),
      validateLarCodeOrEmptyField(RaceEnum, race2, raceEnum),
      validateLarCodeOrEmptyField(RaceEnum, race3, raceEnum),
      validateLarCodeOrEmptyField(RaceEnum, race4, raceEnum),
      validateLarCodeOrEmptyField(RaceEnum, race5, raceEnum),
      validateStr(otherNative),
      validateStr(otherAsian),
      validateStr(otherPacific),
      validateLarCode(RaceObservedEnum, raceObserved, raceObsEnum)
    ).mapN(Race)
  }

  private def validateSex(
      sex: String,
      sexObserved: String,
      coApp: Boolean
  ): LarParserValidationResult[Sex] = {
    val sexEnum = if (coApp) InvalidCoApplicantSex else InvalidApplicantSex
    val sexObsEnum =
      if (coApp) InvalidCoApplicantSexObserved else InvalidApplicantSexObserved
    (
      validateLarCode(SexEnum, sex, sexEnum),
      validateLarCode(SexObservedEnum, sexObserved, sexObsEnum)
    ).mapN(Sex)
  }

}

object ApplicantFormatValidator extends ApplicantFormatValidator
