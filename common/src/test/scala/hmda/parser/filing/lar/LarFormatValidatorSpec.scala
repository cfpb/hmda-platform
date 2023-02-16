package hmda.parser.filing.lar

import cats.data.NonEmptyList
import cats.data.Validated.{ Invalid, Valid }
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import hmda.parser.filing.lar.LarFormatValidator._
import hmda.parser.filing.lar.LarParserErrorModel._
import hmda.parser.filing.lar.LarValidationUtils._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class LarFormatValidatorSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val config = ConfigFactory.load()

  val currentYear    = config.getString("hmda.filing.current")
  val numberOfFields = config.getInt(s"hmda.filing.$currentYear.lar.length")

  property("Loan Application Register must have the correct number of fields") {
    val values = List("a", "b", "c")
    validateLar(values) mustBe Invalid(
      NonEmptyList.of(IncorrectNumberOfFieldsLar(values.length.toString))
    )
  }

  property("Loan Application Register must report InvalidId for non numeric field value") {
    forAll(larGen) { lar =>
      val larId = lar.larIdentifier
      val badId = badValue()
      validateLarIdentifier(badId, larId.LEI, larId.NMLSRIdentifier) mustBe Invalid(NonEmptyList.of(InvalidLarId(badId)))
    }
  }

  property("Invalid Application Date(Bad Leap Year Test)") {
    forAll(larGen) { lar =>
      val badAppDate = badLeapYearValue()
      validateDateOrNaField(badAppDate, InvalidApplicationDate(badAppDate)) mustBe Invalid(
        NonEmptyList.of(InvalidApplicationDate(badAppDate))
      )
    }
  }

  property("Invalid Application Date(Bad Year Value Test 1)") {
    forAll(larGen) { lar =>
      val badAppDate = badDateOptionOneValue()
      validateDateOrNaField(badAppDate, InvalidApplicationDate(badAppDate)) mustBe Invalid(
        NonEmptyList.of(InvalidApplicationDate(badAppDate))
      )
    }
  }

  property("Invalid Application Date(Bad Year Value Test 2)") {
    forAll(larGen) { lar =>
      val badAppDate = badDateOptionTwoValue()
      validateDateOrNaField(badAppDate, InvalidApplicationDate(badAppDate)) mustBe Invalid(
        NonEmptyList.of(InvalidApplicationDate(badAppDate))
      )
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
    validateLarCode(LoanPurposeEnum, loanPurpose, InvalidLoanPurpose(loanPurpose)) mustBe Invalid(
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
    validateLarCode(ConstructionMethodEnum, constructionMethod, InvalidConstructionMethod(constructionMethod)) mustBe Invalid(
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
    validateLarCode(ActionTakenTypeEnum, actionTaken, InvalidActionTaken(actionTaken)) mustBe Invalid(
      NonEmptyList.of(InvalidActionTaken(actionTaken))
    )
  }

  property("InvalidActionTakenDate (Bad Day Test One)") {
    val actionTakenDate = badLeapYearValue()
    validateDateOrNaField(actionTakenDate, InvalidActionTakenDate(actionTakenDate)) mustBe Invalid(
      NonEmptyList.of(InvalidActionTakenDate(actionTakenDate))
    )
  }

  property("InvalidActionTakenDate (Bad Year Value Test 1)") {
    val actionTakenDate = badDateOptionOneValue()
    validateDateOrNaField(actionTakenDate, InvalidActionTakenDate(actionTakenDate)) mustBe Invalid(
      NonEmptyList.of(InvalidActionTakenDate(actionTakenDate))
    )
  }

  property("InvalidActionTakenDate (Bad Year Value Test 2)") {
    val actionTakenDate = badDateOptionTwoValue()
    validateDateOrNaField(actionTakenDate, InvalidActionTakenDate(actionTakenDate)) mustBe Invalid(
      NonEmptyList.of(InvalidActionTakenDate(actionTakenDate))
    )
  }

  property("InvalidAmount") {
    val invalidAmount = badValue()
    validateDoubleField(invalidAmount, InvalidAmount(invalidAmount)) mustBe Invalid(
      NonEmptyList.of(InvalidAmount(invalidAmount))
    )
  }
  property("InvalidLoanTerm") {
    val invalidLoanTerm = badValue()
    validateIntStrOrNAField(invalidLoanTerm, InvalidLoanTerm(invalidLoanTerm)) mustBe Invalid(
      NonEmptyList.of(InvalidLoanTerm(invalidLoanTerm))
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
    val invalidIncome = badValue()
    validateIntStrOrNAField(invalidIncome, InvalidIncome(invalidIncome)) mustBe Invalid(
      NonEmptyList.of(InvalidIncome(invalidIncome))
    )
  }
  property("InvalidPurchaserType") {
    val invalidPurchaserType = badValue()
    validateLarCode(PurchaserEnum, invalidPurchaserType, InvalidPurchaserType(invalidPurchaserType)) mustBe Invalid(
      NonEmptyList.of(InvalidPurchaserType(invalidPurchaserType))
    )
  }
  property("InvalidRateSpread") {
    val invalidRateSpread = badValue()
    validateDoubleStrOrNAField(invalidRateSpread, InvalidRateSpread(invalidRateSpread)) mustBe Invalid(
      NonEmptyList.of(InvalidRateSpread(invalidRateSpread))
    )
  }
  property("InvalidHoepaStatus") {
    val invalidHoepaStatus = badValue()
    validateLarCode(HOEPAStatusEnum, invalidHoepaStatus, InvalidHoepaStatus(invalidHoepaStatus)) mustBe Invalid(
      NonEmptyList.of(InvalidHoepaStatus(invalidHoepaStatus))
    )
  }
  property("InvalidLienStatus") {
    val invalidLienStatus = badValue()
    validateLarCode(LienStatusEnum, invalidLienStatus, InvalidLienStatus(invalidLienStatus)) mustBe Invalid(
      NonEmptyList.of(InvalidLienStatus(invalidLienStatus))
    )
  }
  property("Denial Reason 1") {
    val invalidDenial = badValue()
    validateLarCode(DenialReasonEnum, invalidDenial, InvalidDenial(1, invalidDenial)) mustBe Invalid(
      NonEmptyList.of(InvalidDenial(1, invalidDenial))
    )
  }

  property("Denial Reason 2-4") {
    val invalidDenial = badValue()
    validateLarCodeOrEmptyField(DenialReasonEnum, invalidDenial, InvalidDenial(1, invalidDenial)) mustBe Invalid(
      NonEmptyList.of(InvalidDenial(1, invalidDenial))
    )
    validateLarCodeOrEmptyField(DenialReasonEnum, "-1", InvalidDenial(1, "-1")).toOption.get.getClass mustBe
      (new InvalidDenialReasonCode).getClass
  }
  property("InvalidTotalLoanCosts") {
    val invalidTotalLoanCosts = badValue()
    validateDoubleStrOrNAField(invalidTotalLoanCosts, InvalidTotalLoanCosts(invalidTotalLoanCosts)) mustBe Invalid(
      NonEmptyList.of(InvalidTotalLoanCosts(invalidTotalLoanCosts))
    )
  }
  property("InvalidPointsAndFees") {
    val invalidPointsandFeeds = badValue()
    validateDoubleStrOrNAField(invalidPointsandFeeds, InvalidPointsAndFees(invalidPointsandFeeds)) mustBe Invalid(
      NonEmptyList.of(InvalidPointsAndFees(invalidPointsandFeeds))
    )
  }
  property("InvalidOriginationCharges") {
    val invalidOriginationCharges = badValue()
    validateDoubleStrOrNAField(invalidOriginationCharges, InvalidOriginationCharges(invalidOriginationCharges)) mustBe Invalid(
      NonEmptyList.of(InvalidOriginationCharges(invalidOriginationCharges))
    )
  }
  property("InvalidDiscountPoints") {
    val invalidDiscountPoints = badValue()
    validateDoubleStrOrNAField(invalidDiscountPoints, InvalidDiscountPoints(invalidDiscountPoints)) mustBe Invalid(
      NonEmptyList.of(InvalidDiscountPoints(invalidDiscountPoints))
    )
  }
  property("InvalidLenderCredits") {
    val invalidLenderCredits = badValue()
    validateDoubleStrOrNAField(invalidLenderCredits, InvalidLenderCredits(invalidLenderCredits)) mustBe Invalid(
      NonEmptyList.of(InvalidLenderCredits(invalidLenderCredits))
    )
  }
  property("InvalidInterestRate") {
    val invalidInterestRate = badValue()
    validateDoubleStrOrNAField(invalidInterestRate, InvalidInterestRate(invalidInterestRate)) mustBe Invalid(
      NonEmptyList.of(InvalidInterestRate(invalidInterestRate))
    )
  }
  property("InvalidPrepaymentPenaltyTerm") {
    val invalidPrepaymentPenaltyTerm = badValue()
    validateIntStrOrNAField(invalidPrepaymentPenaltyTerm, InvalidPrepaymentPenaltyTerm(invalidPrepaymentPenaltyTerm)) mustBe Invalid(
      NonEmptyList.of(InvalidPrepaymentPenaltyTerm(invalidPrepaymentPenaltyTerm))
    )
  }
  property("InvalidDebtToIncomeRatio") {
    val invalidDebtToIncomeRatio = badValue()
    validateDoubleStrOrNAField(invalidDebtToIncomeRatio, InvalidDebtToIncomeRatio(invalidDebtToIncomeRatio)) mustBe Invalid(
      NonEmptyList.of(InvalidDebtToIncomeRatio(invalidDebtToIncomeRatio))
    )
  }
  property("Valid Debt to Income Ratio") {
    validateDoubleStrOrNAField("4.125", InvalidDebtToIncomeRatio("4.125")) mustBe Valid("4.125")
  }
  property("InvalidLoanToValueRatio") {
    val invalidLoanToValueRatio = badValue()
    validateDoubleStrOrNAField(invalidLoanToValueRatio, InvalidLoanToValueRatio(invalidLoanToValueRatio)) mustBe Invalid(
      NonEmptyList.of(InvalidLoanToValueRatio(invalidLoanToValueRatio))
    )
  }
  property("InvalidIntroductoryRatePeriod") {
    val invalidntroductoryRatePeriod = badValue()
    validateIntStrOrNAField(invalidntroductoryRatePeriod, InvalidIntroductoryRatePeriod(invalidntroductoryRatePeriod)) mustBe Invalid(
      NonEmptyList.of(InvalidIntroductoryRatePeriod(invalidntroductoryRatePeriod))
    )
  }
  property("InvalidBalloonPayment") {
    val invalidBalloonPayment = badValue()
    validateLarCode(BalloonPaymentEnum, invalidBalloonPayment, InvalidBalloonPayment(invalidBalloonPayment)) mustBe Invalid(
      NonEmptyList.of(InvalidBalloonPayment(invalidBalloonPayment))
    )
  }
  property("InvalidInterestOnlyPayment") {
    val invalidInterestOnlyPayment = badValue()
    validateLarCode(InterestOnlyPaymentsEnum, invalidInterestOnlyPayment, InvalidInterestOnlyPayment(invalidInterestOnlyPayment)) mustBe Invalid(
      NonEmptyList.of(InvalidInterestOnlyPayment(invalidInterestOnlyPayment))
    )
  }
  property("InvalidNegativeAmortization") {
    val invalidNegativeAmortization = badValue()
    validateLarCode(NegativeAmortizationEnum, invalidNegativeAmortization, InvalidNegativeAmortization(invalidNegativeAmortization)) mustBe Invalid(
      NonEmptyList.of(InvalidNegativeAmortization(invalidNegativeAmortization))
    )
  }
  property("InvalidOtherNonAmortizingFeatures") {
    val invalidOtherNonAmortizingFeatures = badValue()
    validateLarCode(
      OtherNonAmortizingFeaturesEnum,
      invalidOtherNonAmortizingFeatures,
      InvalidOtherNonAmortizingFeatures(invalidOtherNonAmortizingFeatures)
    ) mustBe Invalid(
      NonEmptyList.of(InvalidOtherNonAmortizingFeatures(invalidOtherNonAmortizingFeatures))
    )
  }
  property("InvalidPropertyValue") {
    validateIntStrOrNAField("", InvalidPropertyValue("")) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue(""))
    )
    val invalidPropertyValue = badValue()
    validateIntStrOrNAField(invalidPropertyValue, InvalidPropertyValue(invalidPropertyValue)) mustBe Invalid(
      NonEmptyList.of(InvalidPropertyValue(invalidPropertyValue))
    )
  }
  property("InvalidManufacturedHomeSecuredProperty") {
    val invalidManufacturedHomeSecuredProperty = badValue()
    validateLarCode(
      ManufacturedHomeSecuredPropertyEnum,
      invalidManufacturedHomeSecuredProperty,
      InvalidManufacturedHomeSecuredProperty(invalidManufacturedHomeSecuredProperty)
    ) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeSecuredProperty(invalidManufacturedHomeSecuredProperty))
    )
  }
  property("InvalidManufacturedHomeLandPropertyInterest") {
    val invalidManufacturedHomeLandPropertyInterest = badValue()
    validateLarCode(
      ManufacturedHomeLandPropertyInterestEnum,
      invalidManufacturedHomeLandPropertyInterest,
      InvalidManufacturedHomeLandPropertyInterest(invalidManufacturedHomeLandPropertyInterest)
    ) mustBe Invalid(
      NonEmptyList.of(InvalidManufacturedHomeLandPropertyInterest(invalidManufacturedHomeLandPropertyInterest))
    )
  }
  property("InvalidTotalUnits") {
    val invalidTotalUnits = badValue()
    validateIntField(invalidTotalUnits, InvalidTotalUnits(invalidTotalUnits)) mustBe Invalid(
      NonEmptyList.of(InvalidTotalUnits(invalidTotalUnits))
    )
  }
  property("InvalidMultifamilyUnits") {
    val invalidMultifamilyUnits = badValue()
    validateIntStrOrNAField(invalidMultifamilyUnits, InvalidMultifamilyUnits(invalidMultifamilyUnits)) mustBe Invalid(
      NonEmptyList.of(InvalidMultifamilyUnits(invalidMultifamilyUnits))
    )
  }
  property("InvalidApplicationSubmission") {
    val invalidApplicationSubmission = badValue()
    validateLarCode(ApplicationSubmissionEnum, invalidApplicationSubmission, InvalidApplicationSubmission(invalidApplicationSubmission)) mustBe Invalid(
      NonEmptyList.of(InvalidApplicationSubmission(invalidApplicationSubmission))
    )
  }
  property("InvalidPayableToInstitution") {
    val invalidPayableToInstitution = badValue()
    validateLarCode(PayableToInstitutionEnum, invalidPayableToInstitution, InvalidPayableToInstitution(invalidPayableToInstitution)) mustBe Invalid(
      NonEmptyList.of(InvalidPayableToInstitution(invalidPayableToInstitution))
    )
  }
  property("InvalidNMLSRIdentifier") {
    val invalidNMLSRIdentifier = badValue()
    validateIntStrOrNAField(invalidNMLSRIdentifier, InvalidNMLSRIdentifier(invalidNMLSRIdentifier)) mustBe Invalid(
      NonEmptyList.of(InvalidNMLSRIdentifier(invalidNMLSRIdentifier))
    )
  }
  property("InvalidAutomatedUnderwritingSystem 1") {
    val invalidAUSSystem = badValue()
    validateLarCode(AutomatedUnderwritingSystemEnum, invalidAUSSystem, InvalidAutomatedUnderwritingSystem(1, invalidAUSSystem)) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, invalidAUSSystem))
    )
    validateLarCode(AutomatedUnderwritingResultEnum, "2", InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(AutomatedUnderwritingResultEnum, "", InvalidAutomatedUnderwritingSystem(1, "")) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, ""))
    )
  }

  property("InvalidAutomatedUnderwritingSystem 2 - 5") {
    val invalidAUSSystem = badValue()
    validateLarCodeOrEmptyField(AutomatedUnderwritingSystemEnum, invalidAUSSystem, InvalidAutomatedUnderwritingSystem(1, invalidAUSSystem)) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystem(1, invalidAUSSystem))
    )
    validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, "-1", InvalidAutomatedUnderwritingSystem(1, "-1")).toOption.get.getClass mustBe (new InvalidAutomatedUnderwritingResultCode).getClass

  }
  property("InvalidAutomatedUnderwritingSystemResult 1") {
    val invalidAUSResult = badValue()
    validateLarCode(AutomatedUnderwritingResultEnum, invalidAUSResult, InvalidAutomatedUnderwritingSystemResult(1, invalidAUSResult)) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, invalidAUSResult))
    )
    validateLarCode(AutomatedUnderwritingResultEnum, "2", InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCode(AutomatedUnderwritingResultEnum, "", InvalidAutomatedUnderwritingSystemResult(1, "")) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, ""))
    )
  }

  property("InvalidAutomatedUnderwritingSystemResult 2-5") {
    val invalidAUSResult = badValue()
    validateLarCodeOrEmptyField(
      AutomatedUnderwritingResultEnum,
      invalidAUSResult,
      InvalidAutomatedUnderwritingSystemResult(1, invalidAUSResult)
    ) mustBe Invalid(
      NonEmptyList.of(InvalidAutomatedUnderwritingSystemResult(1, invalidAUSResult))
    )
    validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, "2", InvalidAutomatedUnderwritingSystem(1, "2")) mustBe Valid(
      ApproveIneligible
    )
    validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, "-1", InvalidAutomatedUnderwritingSystemResult(1, "-1")).toOption.get.getClass mustBe (new InvalidAutomatedUnderwritingResultCode).getClass
  }
  property("InvalidMortgageType") {
    val invalidMortgageType = badValue()
    validateLarCode(MortgageTypeEnum, invalidMortgageType, InvalidMortgageType(invalidMortgageType)) mustBe Invalid(
      NonEmptyList.of(InvalidMortgageType(invalidMortgageType))
    )
  }
  property("InvalidLineOfCredit") {
    val invalidLineOfCredit = badValue()
    validateLarCode(LineOfCreditEnum, invalidLineOfCredit, InvalidLineOfCredit(invalidLineOfCredit)) mustBe Invalid(
      NonEmptyList.of(InvalidLineOfCredit(invalidLineOfCredit))
    )
  }
  property("InvalidBusinessOrCommercial") {
    val invalidCommercialType = badValue()
    validateLarCode(BusinessOrCommercialBusinessEnum, invalidCommercialType, InvalidBusinessOrCommercial(invalidCommercialType)) mustBe Invalid(
      NonEmptyList.of(InvalidBusinessOrCommercial(invalidCommercialType))
    )
  }

}