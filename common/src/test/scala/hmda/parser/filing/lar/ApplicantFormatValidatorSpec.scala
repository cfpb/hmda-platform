package hmda.parser.filing.lar

import cats.data.NonEmptyList
import cats.data.Validated.Invalid
import hmda.model.filing.lar.Applicant
import hmda.model.filing.lar.LarGenerators._
import hmda.parser.LarParserValidationResult
import hmda.parser.filing.lar.ApplicantFormatValidator._
import hmda.parser.filing.lar.LarParserErrorModel._
import hmda.parser.filing.lar.LarValidationUtils._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class ApplicantFormatValidatorSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Applicant must report Invalid Ethnicity for non numeric ethnicity field") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant).updated(0, "a")
      validateApplicantValues(badValues) mustBe Invalid(NonEmptyList.of(InvalidApplicantEthnicity(1, "a")))
    }
  }

  property("Applicant must report Invalid Race for non numeric race field") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant).updated(7, "a")
      validateApplicantValues(badValues) mustBe Invalid(NonEmptyList.of(InvalidApplicantRace(1, "a")))
    }
  }

  property("Applicant must report Invalid Sex for non numeric sex field") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant).updated(16, "o")
      validateApplicantValues(badValues) mustBe Invalid(NonEmptyList.of(InvalidApplicantSex("o")))
    }
  }

  property("Applicant must report Invalid Age for non numeric age field") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant).updated(18, "xx")
      validateApplicantValues(badValues) mustBe Invalid(
        NonEmptyList.of(InvalidApplicantAge("xx"))
      )
    }
  }

  property("Applicant must report Invalid Credit Score for non numeric field") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant).updated(20, "a")
      validateApplicantValues(badValues) mustBe Invalid(
        NonEmptyList.of(InvalidApplicantCreditScoreModel("a"))
      )
    }
  }

  property("Applicant must accumulate parsing errors") {
    forAll(larGen) { lar =>
      val applicant = lar.applicant
      val badValues = extractValues(applicant)
        .updated(0, "a")
        .updated(7, "b")
        .updated(16, "oh")
      validateApplicantValues(badValues) mustBe Invalid(
        NonEmptyList.of(InvalidApplicantEthnicity(1, "a"), InvalidApplicantRace(1, "b"), InvalidApplicantSex("oh"))
      )
    }
  }

  private def validateApplicantValues(values: Seq[String]): LarParserValidationResult[Applicant] = {
    val ethnicity1            = values(0)
    val ethnicity2            = values(1)
    val ethnicity3            = values(2)
    val ethnicity4            = values(3)
    val ethnicity5            = values(4)
    val otherHispanicOrLatino = values(5)
    val ethnicityObserved     = values(6)
    val race1                 = values(7)
    val race2                 = values(8)
    val race3                 = values(9)
    val race4                 = values(10)
    val race5                 = values(11)
    val otherNative           = values(12)
    val otherAsian            = values(13)
    val otherPacific          = values(14)
    val raceObserved          = values(15)
    val sex                   = values(16)
    val sexObserved           = values(17)
    val age                   = values(18)
    val creditScore           = values(19)
    val creditScoreModel      = values(20)
    val otherCreditScore      = values(21)
    validateApplicant(
      ethnicity1,
      ethnicity2,
      ethnicity3,
      ethnicity4,
      ethnicity5,
      otherHispanicOrLatino,
      ethnicityObserved,
      race1,
      race2,
      race3,
      race4,
      race5,
      otherNative,
      otherAsian,
      otherPacific,
      raceObserved,
      sex,
      sexObserved,
      age,
      creditScore,
      creditScoreModel,
      otherCreditScore,
      coApp = false
    )
  }

}