package hmda.parser.filing.lar

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import hmda.parser.ParserErrorModel.IncorrectNumberOfFields
import hmda.parser.filing.lar.LarFormatValidator._
import hmda.parser.filing.lar.LarParserErrorModel._
import hmda.parser.filing.lar.LarValidationUtils._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

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
      validateIntStrOrNAField(badAppDate, InvalidApplicationDate) mustBe Invalid(
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
    validateIntStrOrNAField(badValue(), InvalidLoanTerm) mustBe Invalid(
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
        loan.combinedLoanToValueRatio,
        loan.introductoryRatePeriod
      ) mustBe Valid(loan)
    }
  }

  property("A Denial must validate its format") {
    forAll(larGen) { lar =>
      val denial = lar.denial
      validateDenial(
        denial.denialReason1.code.toString,
        denial.denialReason2.code.toString,
        denial.denialReason3.code.toString,
        denial.denialReason4.code.toString,
        denial.otherDenialReason
      ) mustBe Valid(denial)
    }
  }

  property("InvalidIncome") {
    validateIntStrOrNAField(badValue(), InvalidIncome) mustBe Invalid(
      NonEmptyList.of(InvalidIncome)
    )
  }
  property("InvalidPurchaserType") {
    validateLarCode(PurchaserEnum, badValue(), InvalidPurchaserType) mustBe Invalid(
      NonEmptyList.of(InvalidPurchaserType)
    )
  }
  property("InvalidRateSpread") {
    validateDoubleStrOrNAField(badValue(), InvalidRateSpread) mustBe Invalid(
      NonEmptyList.of(InvalidRateSpread)
    )
  }
  property("InvalidHoepaStatus") {
    validateLarCode(HOEPAStatusEnum, badValue(), InvalidHoepaStatus) mustBe Invalid(
      NonEmptyList.of(InvalidHoepaStatus)
    )
  }
  property("InvalidLienStatus") {
    validateLarCode(LienStatusEnum, badValue(), InvalidLienStatus) mustBe Invalid(
      NonEmptyList.of(InvalidLienStatus)
    )
  }
  property("Denial Reason 1") {
    validateLarCode(DenialReasonEnum, badValue(), InvalidDenial) mustBe Invalid(
      NonEmptyList.of(InvalidDenial)
    )
  }

  property("Denial Reason 2-4") {
    validateLarCodeOrEmptyField(DenialReasonEnum, badValue(), InvalidDenial) mustBe Invalid(
      NonEmptyList.of(InvalidDenial)
    )
    validateLarCodeOrEmptyField(DenialReasonEnum, "-1", InvalidDenial) mustBe Valid(
      InvalidDenialReasonCode)
  }
  property("InvalidTotalLoanCosts") {
    validateDoubleStrOrNAField(badValue(), InvalidTotalLoanCosts) mustBe Invalid(
      NonEmptyList.of(InvalidTotalLoanCosts)
    )
  }
  property("InvalidPointsAndFees") {
    validateDoubleStrOrNAField(badValue(), InvalidPointsAndFees) mustBe Invalid(
      NonEmptyList.of(InvalidPointsAndFees)
    )
  }
  property("InvalidOriginationCharges") {
    validateDoubleStrOrNAField(badValue(), InvalidOriginationCharges) mustBe Invalid(
      NonEmptyList.of(InvalidOriginationCharges)
    )
  }
  property("InvalidDiscountPoints") {
    validateDoubleStrOrNAField(badValue(), InvalidDiscountPoints) mustBe Invalid(
      NonEmptyList.of(InvalidDiscountPoints)
    )
  }
  property("InvalidLenderCredits") {
    validateDoubleStrOrNAField(badValue(), InvalidLenderCredits) mustBe Invalid(
      NonEmptyList.of(InvalidLenderCredits)
    )
  }
  property("InvalidInterestRate") {
    validateDoubleStrOrNAField(badValue(), InvalidInterestRate) mustBe Invalid(
      NonEmptyList.of(InvalidInterestRate)
    )
  }
  property("InvalidPrepaymentPenaltyTerm") {
    validateIntStrOrNAField(badValue(), InvalidPrepaymentPenaltyTerm) mustBe Invalid(
      NonEmptyList.of(InvalidPrepaymentPenaltyTerm)
    )
  }
  property("InvalidDebtToIncomeRatio") {
    validateDoubleStrOrNAField(badValue(), InvalidDebtToIncomeRatio) mustBe Invalid(
      NonEmptyList.of(InvalidDebtToIncomeRatio)
    )
  }
  property("Valid Debt to Income Ratio") {
    validateDoubleStrOrNAField("4.125", InvalidDebtToIncomeRatio) mustBe Valid(
      "4.125")
  }
  property("InvalidLoanToValueRatio") {
    validateDoubleStrOrNAField(badValue(), InvalidLoanToValueRatio) mustBe Invalid(
      NonEmptyList.of(InvalidLoanToValueRatio)
    )
  }
  property("InvalidIntroductoryRatePeriod") {
    validateIntStrOrNAField(badValue(), InvalidIntroductoryRatePeriod) mustBe Invalid(
      NonEmptyList.of(InvalidIntroductoryRatePeriod)
    )
  }
  property("InvalidBalloonPayment") {
    validateLarCode(BalloonPaymentEnum, badValue(), InvalidBalloonPayment) mustBe Invalid(
      NonEmptyList.of(InvalidBalloonPayment)
    )
  }
  property("InvalidInterestOnlyPayment") {
    validateLarCode(InterestOnlyPaymentsEnum,
                    badValue(),
                    InvalidInterestOnlyPayment) mustBe Invalid(
      NonEmptyList.of(InvalidInterestOnlyPayment)
    )
  }
  property("InvalidNegativeAmortization") {
    validateLarCode(NegativeAmortizationEnum,
                    badValue(),
                    InvalidNegativeAmortization) mustBe Invalid(
      NonEmptyList.of(InvalidNegativeAmortization)
    )
  }
  property("InvalidOtherNonAmortizingFeatures") {
    validateLarCode(OtherNonAmortizingFeaturesEnum,
                    badValue(),
                    InvalidOtherNonAmortizingFeatures) mustBe Invalid(
      NonEmptyList.of(InvalidOtherNonAmortizingFeatures)
    )
  }
  property("InvalidPropertyValue") {
    validateIntStrOrNAField("", InvalidPropertyValue) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue)
    )
    validateIntStrOrNAField(badValue(), InvalidPropertyValue) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue)
    )
  }
  property("InvalidManufacturedHomeSecuredProperty") {
    validateLarCode(ManufacturedHomeSecuredPropertyEnum,
                    badValue(),
                    InvalidManufacturedHomeSecuredProperty) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeSecuredProperty)
    )
  }
  property("InvalidManufacturedHomeLandPropertyInterest") {
    validateLarCode(ManufacturedHomeLandPropertyInterestEnum,
                    badValue(),
                    InvalidManufacturedHomeLandPropertyInterest) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeLandPropertyInterest)
    )
  }
  property("InvalidTotalUnits") {
    validateIntField(badValue(), InvalidTotalUnits) mustBe Invalid(
      NonEmptyList.of(InvalidTotalUnits)
    )
  }
  property("InvalidMultifamilyUnits") {
    validateIntStrOrNAField(badValue(), InvalidMultifamilyUnits) mustBe Invalid(
      NonEmptyList.of(InvalidMultifamilyUnits)
    )
  }
  property("InvalidApplicationSubmission") {
    validateLarCode(ApplicationSubmissionEnum,
                    badValue(),
                    InvalidApplicationSubmission) mustBe Invalid(
      NonEmptyList.of(InvalidApplicationSubmission)
    )
  }
  property("InvalidPayableToInstitution") {
    validateLarCode(PayableToInstitutionEnum,
                    badValue(),
                    InvalidPayableToInstitution) mustBe Invalid(
      NonEmptyList.of(InvalidPayableToInstitution)
    )
  }
  property("InvalidNMLSRIdentifier") {
    validateIntStrOrNAField(badValue(), InvalidNMLSRIdentifier) mustBe Invalid(
      NonEmptyList.of(InvalidNMLSRIdentifier)
    )
  }
  property("InvalidAutomatedUnderwritingSystem 1") {
    validateLarCode(AutomatedUnderwritingSystemEnum,
                    badValue(),
                    InvalidAutomatedUnderwritingSystem) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem)
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "2",
                    InvalidAutomatedUnderwritingSystem) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "",
                    InvalidAutomatedUnderwritingSystem) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem)
    )
  }

  property("InvalidAutomatedUnderwritingSystem 2 - 5") {
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingSystemEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystem) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem)
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "-1",
      InvalidAutomatedUnderwritingSystem) mustBe Valid(
      InvalidAutomatedUnderwritingResultCode)

  }
  property("InvalidAutomatedUnderwritingSystemResult 1") {
    validateLarCode(AutomatedUnderwritingResultEnum,
                    badValue(),
                    InvalidAutomatedUnderwritingSystemResult) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult)
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "2",
                    InvalidAutomatedUnderwritingSystem) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "",
                    InvalidAutomatedUnderwritingSystemResult) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult))
  }

  property("InvalidAutomatedUnderwritingSystemResult 2-5") {
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystemResult) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult)
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "2",
      InvalidAutomatedUnderwritingSystem) mustBe Valid(
      ApproveIneligible
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "-1",
      InvalidAutomatedUnderwritingSystemResult) mustBe Valid(
      InvalidAutomatedUnderwritingResultCode)
  }
  property("InvalidMortgageType") {
    validateLarCode(MortgageTypeEnum, badValue(), InvalidMortgageType) mustBe Invalid(
      NonEmptyList.of(InvalidMortgageType))
  }
  property("InvalidLineOfCredit") {
    validateLarCode(LineOfCreditEnum, badValue(), InvalidLineOfCredit) mustBe Invalid(
      NonEmptyList.of(InvalidLineOfCredit))
  }
  property("InvalidBusinessOrCommercial") {
    validateLarCode(BusinessOrCommercialBusinessEnum,
                    badValue(),
                    InvalidBusinessOrCommercial) mustBe Invalid(
      NonEmptyList.of(InvalidBusinessOrCommercial))
  }

}
