package hmda.parser.filing.lar

import hmda.model.filing.lar.{Applicant, Ethnicity, Race, Sex}
import hmda.model.filing.lar.enums._
import hmda.parser.filing.lar.LarParserErrorModel._
import cats.implicits._

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
      otherCreditScore: String
  ): LarParserValidationResult[Applicant] = {
    (
      validateEthnicity(ethnicity1,
                        ethnicity2,
                        ethnicity3,
                        ethnicity4,
                        ethnicity5,
                        otherHispanicOrLatino,
                        ethnicityObserved),
      validateRace(race1,
                   race2,
                   race3,
                   race4,
                   race5,
                   otherNative,
                   otherAsian,
                   otherPacific,
                   raceObserved),
      validateSex(sex, sexObserved),
      validateIntField(age, InvalidAge),
      validateIntField(creditScore, InvalidCreditScore),
      validateLarCode(CreditScoreEnum, creditScoreModel, InvalidCreditScore),
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
      ethnicityObserved: String
  ): LarParserValidationResult[Ethnicity] = {
    (
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity1, InvalidEthnicity),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity2, InvalidEthnicity),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity3, InvalidEthnicity),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity4, InvalidEthnicity),
      validateLarCodeOrEmptyField(EthnicityEnum, ethnicity5, InvalidEthnicity),
      validateStr(otherHispanicOrLatino),
      validateLarCode(EthnicityObservedEnum,
                      ethnicityObserved,
                      InvalidEthnicity)
    ).mapN(Ethnicity)
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
      raceObserved: String): LarParserValidationResult[Race] = {

    (
      validateLarCodeOrEmptyField(RaceEnum, race1, InvalidRace),
      validateLarCodeOrEmptyField(RaceEnum, race2, InvalidRace),
      validateLarCodeOrEmptyField(RaceEnum, race3, InvalidRace),
      validateLarCodeOrEmptyField(RaceEnum, race4, InvalidRace),
      validateLarCodeOrEmptyField(RaceEnum, race5, InvalidRace),
      validateStr(otherNative),
      validateStr(otherAsian),
      validateStr(otherPacific),
      validateLarCode(RaceObservedEnum, raceObserved, InvalidRace)
    ).mapN(Race)
  }

  private def validateSex(
      sex: String,
      sexObserved: String
  ): LarParserValidationResult[Sex] = {
    (
      validateLarCode(SexEnum, sex, InvalidSex),
      validateLarCode(SexObservedEnum, sexObserved, InvalidSex)
    ).mapN(Sex)
  }

}

object ApplicantFormatValidator extends ApplicantFormatValidator
