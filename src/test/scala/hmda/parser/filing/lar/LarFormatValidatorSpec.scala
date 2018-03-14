package hmda.parser.filing.lar

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import LarFormatValidator._
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import hmda.parser.ParserErrorModel.IncorrectNumberOfFields
import hmda.model.filing.lar.LarGenerators._
import LarValidationUtils._
import hmda.model.filing.lar.enums._
import hmda.parser.filing.lar.LarParserErrorModel._

class LarFormatValidatorSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  val config = ConfigFactory.load()

  val numberOfFields = config.getInt("hmda.filing.lar.length")

  property("Loan Application Register must have the correct number of fields") {
    val values = List("a", "b", "c")
    validateLar(values) mustBe Invalid(
      NonEmptyList.of(IncorrectNumberOfFields(values.length, numberOfFields))
    )
  }

  property(
    "Loan Application Register must report InvalidId for non numeric field value") {
    forAll(larGen) { lar =>
      val larId = lar.larIdentifier
      val badId = badValue()
      validateLarIdentifier(badId, larId.LEI, larId.NMLSRIdentifier) mustBe Invalid(
        NonEmptyList.of(InvalidId))
    }
  }

  property("Invalid Application Date") {
    forAll(larGen) { lar =>
      val badAppDate = badValue()
      validateStrOrNAField(badAppDate, InvalidApplicationDate) mustBe Invalid(
        NonEmptyList.of(InvalidApplicationDate))
    }
  }

  property("InvalidLoanType") {
    val loanType = badValue()
    validateLarCode(LoanTypeEnum, loanType, InvalidLoanType) mustBe Invalid(
      NonEmptyList.of(InvalidLoanType)
    )
  }

  property("InvalidLoanPurpose") {
    val loanPurpose = badValue()
    validateLarCode(LoanPurposeEnum, loanPurpose, InvalidLoanPurpose) mustBe Invalid(
      NonEmptyList.of(InvalidLoanPurpose)
    )
  }

  property("InvalidPreapproval") {
    val preapproval = badValue()
    validateIntField(preapproval, InvalidPreapproval) mustBe Invalid(
      NonEmptyList.of(InvalidPreapproval)
    )
  }
  property("InvalidConstructionMethod") {
    val constructionMethod = badValue()
    validateLarCode(ConstructionMethodEnum,
                    constructionMethod,
                    InvalidConstructionMethod) mustBe Invalid(
      NonEmptyList.of(InvalidConstructionMethod)
    )
  }
  property("InvalidOccupancy") {
    val occupancy = badValue()
    validateLarCode(OccupancyEnum, occupancy, InvalidOccupancy) mustBe Invalid(
      NonEmptyList.of(InvalidOccupancy)
    )
  }
  property("InvalidActionTaken") {
    val actionTaken = badValue()
    validateLarCode(ActionTakenTypeEnum, actionTaken, InvalidActionTaken) mustBe Invalid(
      NonEmptyList.of(InvalidActionTaken)
    )
  }
  property("InvalidActionTakenDate") {
    validateIntField(badValue(), InvalidActionTakenDate) mustBe Invalid(
      NonEmptyList.of(InvalidActionTakenDate)
    )
  }
  property("InvalidAmount") {
    validateDoubleField(badValue(), InvalidAmount) mustBe Invalid(
      NonEmptyList.of(InvalidAmount)
    )
  }
  property("InvalidLoanTerm") {
    validateStrOrNAField(badValue(), InvalidLoanTerm) mustBe Invalid(
      NonEmptyList.of(InvalidLoanTerm)
    )
  }

  property("A Loan must validate its format") {
    forAll(larGen) { lar =>
      val loan = lar.loan
      validateLoan(
        loan.ULI,
        loan.applicationDate,
        loan.loanType.code.toString,
        loan.loanPurpose.code.toString,
        loan.constructionMethod.code.toString,
        loan.occupancy.code.toString,
        loan.amount.toString,
        loan.loanTerm,
        loan.rateSpread,
        loan.interestRate,
        loan.prepaymentPenaltyTerm,
        loan.debtToIncomeRatio,
        loan.loanToValueRatio,
        loan.introductoryRatePeriod
      ) mustBe Valid(loan)
    }
  }

  property("InvalidIncome") {
    validateStrOrNAField(badValue(), InvalidIncome) mustBe Invalid(
      NonEmptyList.of(InvalidIncome)
    )
  }
  //  property("InvalidPurchaserType") {
  //    pending
  //  }
  //  property("InvalidRateSpread") {
  //    pending
  //  }
  //  property("InvalidHoepaStatus") {
  //    pending
  //  }
  //  property("InvalidLienStatus") {
  //    pending
  //  }
  //  property("InvalidDenial") {
  //    pending
  //  }
  //  property("InvalidTotalLoanCosts") {
  //    pending
  //  }
  //  property("InvalidPointsAndFees") {
  //    pending
  //  }
  //  property("InvalidOriginationCharges") {
  //    pending
  //  }
  //  property("InvalidDiscountPoints") {
  //    pending
  //  }
  //  property("InvalidLenderCredits") {
  //    pending
  //  }
  //  property("InvalidInterestRate") {
  //    pending
  //  }
  //  property("InvalidPrepaymentPenaltyTerm") {
  //    pending
  //  }
  //  property("InvalidDebtToIncomeRatio") {
  //    pending
  //  }
  //  property("InvalidLoanToValueRatio") {
  //    pending
  //  }
  //  property("InvalidIntroductoryRatePeriod") {
  //    pending
  //  }
  //  property("InvalidBalloonPayment") {
  //    pending
  //  }
  //  property("InvalidInterestOnlyPayment") {
  //    pending
  //  }
  //  property("InvalidNegativeAmortization") {
  //    pending
  //  }
  //  property("InvalidOtherNonAmortizingFeatures") {
  //    pending
  //  }
  //  property("InvalidPropertyValue") {
  //    pending
  //  }
  //  property("InvalidManufacturedHomeSecuredProperty") {
  //    pending
  //  }
  //  property("InvalidManufacturedHomeLandPropertyInterest") {
  //    pending
  //  }
  //  property("InvalidTotalUnits") {
  //    pending
  //  }
  //  property("InvalidMultifamilyUnits") {
  //    pending
  //  }
  //  property("InvalidApplicationSubmission") {
  //    pending
  //  }
  //  property("InvalidPayableToInstitution") {
  //    pending
  //  }
  //  property("InvalidNMLSRIdentifier") {
  //    pending
  //  }
  //  property("InvalidAutomatedUnderwritingSystem") {
  //    pending
  //  }
  //  property("InvalidAutomatedUnderwritingSystemResult") {
  //    pending
  //  }
  //  property("InvalidMortgageType") {
  //    pending
  //  }
  //  property("InvalidLineOfCredit") {
  //    pending
  //  }
  //  property("InvalidBusinessOrCommercial") {
  //    pending
  //  }

}
