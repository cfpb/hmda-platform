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

  val currentYear = config.getString("hmda.filing.current")
  val numberOfFields = config.getInt(s"hmda.filing.$currentYear.lar.length")

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
      validateIntStrOrNAField(badAppDate, InvalidApplicationDate(badAppDate)) mustBe Invalid(
        NonEmptyList.of(InvalidApplicationDate(badAppDate)))
    }
  }

  property("InvalidLoanType") {
    val loanType = badValue()
    validateLarCode(LoanTypeEnum, loanType, InvalidLoanType(loanType)) mustBe Invalid(
      NonEmptyList.of(InvalidLoanType(loanType))
    )
  }

  property("InvalidLoanPurpose") {
    val loanPurpose = badValue()
    validateLarCode(LoanPurposeEnum,
                    loanPurpose,
                    InvalidLoanPurpose(loanPurpose)) mustBe Invalid(
      NonEmptyList.of(InvalidLoanPurpose(loanPurpose))
    )
  }

  property("InvalidPreapproval") {
    val preapproval = badValue()
    validateIntField(preapproval, InvalidPreapproval(preapproval)) mustBe Invalid(
      NonEmptyList.of(InvalidPreapproval(preapproval))
    )
  }
  property("InvalidConstructionMethod") {
    val constructionMethod = badValue()
    validateLarCode(
      ConstructionMethodEnum,
      constructionMethod,
      InvalidConstructionMethod(constructionMethod)) mustBe Invalid(
      NonEmptyList.of(InvalidConstructionMethod(constructionMethod))
    )
  }
  property("InvalidOccupancy") {
    val occupancy = badValue()
    validateLarCode(OccupancyEnum, occupancy, InvalidOccupancy(occupancy)) mustBe Invalid(
      NonEmptyList.of(InvalidOccupancy(occupancy))
    )
  }
  property("InvalidActionTaken") {
    val actionTaken = badValue()
    validateLarCode(ActionTakenTypeEnum,
                    actionTaken,
                    InvalidActionTaken(actionTaken)) mustBe Invalid(
      NonEmptyList.of(InvalidActionTaken(actionTaken))
    )
  }
  property("InvalidActionTakenDate") {
    validateIntField(badValue(), InvalidActionTakenDate(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidActionTakenDate(badValue()))
    )
  }
  property("InvalidAmount") {
    validateDoubleField(badValue(), InvalidAmount(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidAmount(badValue()))
    )
  }
  property("InvalidLoanTerm") {
    validateIntStrOrNAField(badValue(), InvalidLoanTerm(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidLoanTerm(badValue()))
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
    validateIntStrOrNAField(badValue(), InvalidIncome(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidIncome(badValue()))
    )
  }
  property("InvalidPurchaserType") {
    validateLarCode(PurchaserEnum, badValue(), InvalidPurchaserType(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidPurchaserType(badValue()))
    )
  }
  property("InvalidRateSpread") {
    validateDoubleStrOrNAField(badValue(), InvalidRateSpread(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidRateSpread(badValue()))
    )
  }
  property("InvalidHoepaStatus") {
    validateLarCode(HOEPAStatusEnum, badValue(), InvalidHoepaStatus(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidHoepaStatus(badValue()))
    )
  }
  property("InvalidLienStatus") {
    validateLarCode(LienStatusEnum, badValue(), InvalidLienStatus(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidLienStatus(badValue()))
    )
  }
  property("Denial Reason 1") {
    validateLarCode(DenialReasonEnum, badValue(), InvalidDenial(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidDenial(1, badValue()))
    )
  }

  property("Denial Reason 2-4") {
    validateLarCodeOrEmptyField(DenialReasonEnum,
                                badValue(),
                                InvalidDenial(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidDenial(1, badValue()))
    )
    validateLarCodeOrEmptyField(DenialReasonEnum, "-1", InvalidDenial(1, "-1")) mustBe Valid(
      InvalidDenialReasonCode)
  }
  property("InvalidTotalLoanCosts") {
    validateDoubleStrOrNAField(badValue(), InvalidTotalLoanCosts(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidTotalLoanCosts(badValue()))
    )
  }
  property("InvalidPointsAndFees") {
    validateDoubleStrOrNAField(badValue(), InvalidPointsAndFees(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidPointsAndFees(badValue()))
    )
  }
  property("InvalidOriginationCharges") {
    validateDoubleStrOrNAField(
      badValue(),
      InvalidOriginationCharges(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidOriginationCharges(badValue()))
    )
  }
  property("InvalidDiscountPoints") {
    validateDoubleStrOrNAField(badValue(), InvalidDiscountPoints(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidDiscountPoints(badValue()))
    )
  }
  property("InvalidLenderCredits") {
    validateDoubleStrOrNAField(badValue(), InvalidLenderCredits(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidLenderCredits(badValue()))
    )
  }
  property("InvalidInterestRate") {
    validateDoubleStrOrNAField(badValue(), InvalidInterestRate(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidInterestRate(badValue()))
    )
  }
  property("InvalidPrepaymentPenaltyTerm") {
    validateIntStrOrNAField(
      badValue(),
      InvalidPrepaymentPenaltyTerm(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidPrepaymentPenaltyTerm(badValue()))
    )
  }
  property("InvalidDebtToIncomeRatio") {
    validateDoubleStrOrNAField(badValue(), InvalidDebtToIncomeRatio(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidDebtToIncomeRatio(badValue()))
    )
  }
  property("Valid Debt to Income Ratio") {
    validateDoubleStrOrNAField("4.125", InvalidDebtToIncomeRatio("4.125")) mustBe Valid(
      "4.125")
  }
  property("InvalidLoanToValueRatio") {
    validateDoubleStrOrNAField(badValue(), InvalidLoanToValueRatio(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidLoanToValueRatio(badValue()))
    )
  }
  property("InvalidIntroductoryRatePeriod") {
    validateIntStrOrNAField(
      badValue(),
      InvalidIntroductoryRatePeriod(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidIntroductoryRatePeriod(badValue()))
    )
  }
  property("InvalidBalloonPayment") {
    validateLarCode(BalloonPaymentEnum,
                    badValue(),
                    InvalidBalloonPayment(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidBalloonPayment(badValue()))
    )
  }
  property("InvalidInterestOnlyPayment") {
    validateLarCode(InterestOnlyPaymentsEnum,
                    badValue(),
                    InvalidInterestOnlyPayment(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidInterestOnlyPayment(badValue()))
    )
  }
  property("InvalidNegativeAmortization") {
    validateLarCode(NegativeAmortizationEnum,
                    badValue(),
                    InvalidNegativeAmortization(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidNegativeAmortization(badValue()))
    )
  }
  property("InvalidOtherNonAmortizingFeatures") {
    validateLarCode(
      OtherNonAmortizingFeaturesEnum,
      badValue(),
      InvalidOtherNonAmortizingFeatures(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidOtherNonAmortizingFeatures(badValue()))
    )
  }
  property("InvalidPropertyValue") {
    validateIntStrOrNAField("", InvalidPropertyValue("")) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue(""))
    )
    validateIntStrOrNAField(badValue(), InvalidPropertyValue(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue(badValue()))
    )
  }
  property("InvalidManufacturedHomeSecuredProperty") {
    validateLarCode(
      ManufacturedHomeSecuredPropertyEnum,
      badValue(),
      InvalidManufacturedHomeSecuredProperty(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeSecuredProperty(badValue()))
    )
  }
  property("InvalidManufacturedHomeLandPropertyInterest") {
    validateLarCode(
      ManufacturedHomeLandPropertyInterestEnum,
      badValue(),
      InvalidManufacturedHomeLandPropertyInterest(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeLandPropertyInterest(badValue()))
    )
  }
  property("InvalidTotalUnits") {
    validateIntField(badValue(), InvalidTotalUnits(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidTotalUnits(badValue()))
    )
  }
  property("InvalidMultifamilyUnits") {
    validateIntStrOrNAField(badValue(), InvalidMultifamilyUnits(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidMultifamilyUnits(badValue()))
    )
  }
  property("InvalidApplicationSubmission") {
    validateLarCode(ApplicationSubmissionEnum,
                    badValue(),
                    InvalidApplicationSubmission(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidApplicationSubmission(badValue()))
    )
  }
  property("InvalidPayableToInstitution") {
    validateLarCode(PayableToInstitutionEnum,
                    badValue(),
                    InvalidPayableToInstitution(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidPayableToInstitution(badValue()))
    )
  }
  property("InvalidNMLSRIdentifier") {
    validateIntStrOrNAField(badValue(), InvalidNMLSRIdentifier(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidNMLSRIdentifier(badValue()))
    )
  }
  property("InvalidAutomatedUnderwritingSystem 1") {
    validateLarCode(
      AutomatedUnderwritingSystemEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystem(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, badValue()))
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "2",
                    InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "",
                    InvalidAutomatedUnderwritingSystem(1, "")) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, ""))
    )
  }

  property("InvalidAutomatedUnderwritingSystem 2 - 5") {
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingSystemEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystem(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, badValue()))
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "-1",
      InvalidAutomatedUnderwritingSystem(1, "-1")) mustBe Valid(
      InvalidAutomatedUnderwritingResultCode)

  }
  property("InvalidAutomatedUnderwritingSystemResult 1") {
    validateLarCode(
      AutomatedUnderwritingResultEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystemResult(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, badValue()))
    )
    validateLarCode(AutomatedUnderwritingResultEnum,
                    "2",
                    InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(
      AutomatedUnderwritingResultEnum,
      "",
      InvalidAutomatedUnderwritingSystemResult(1, "")) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, "")))
  }

  property("InvalidAutomatedUnderwritingSystemResult 2-5") {
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      badValue(),
      InvalidAutomatedUnderwritingSystemResult(1, badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, badValue()))
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "2",
      InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      "-1",
      InvalidAutomatedUnderwritingSystemResult(1, "-1")) mustBe Valid(
      InvalidAutomatedUnderwritingResultCode)
  }
  property("InvalidMortgageType") {
    validateLarCode(MortgageTypeEnum,
                    badValue(),
                    InvalidMortgageType(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidMortgageType(badValue())))
  }
  property("InvalidLineOfCredit") {
    validateLarCode(LineOfCreditEnum,
                    badValue(),
                    InvalidLineOfCredit(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidLineOfCredit(badValue())))
  }
  property("InvalidBusinessOrCommercial") {
    validateLarCode(BusinessOrCommercialBusinessEnum,
                    badValue(),
                    InvalidBusinessOrCommercial(badValue())) mustBe Invalid(
      NonEmptyList.of(InvalidBusinessOrCommercial(badValue())))
  }

}
