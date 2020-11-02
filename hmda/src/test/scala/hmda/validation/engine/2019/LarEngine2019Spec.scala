package hmda.validation.engine

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.{ Ethnicity, LoanApplicationRegister, Race }
import hmda.model.validation.TsValidationError
import hmda.utils.YearUtils.Period
import hmda.validation.context.ValidationContext
import hmda.validation.engine.LarEngine2019._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ MustMatchers, WordSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LarEngine2019Spec extends WordSpec with ScalaCheckPropertyChecks with MustMatchers with Eventually {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  "Lar Validation Engine must pass all checks for a valid LoanApplicationRegister" in {
    eventually {
      lazy val lar: LoanApplicationRegister = larGen.sample.fold(ifEmpty = lar)(identity)
      val fixedLar =
        lar.copy(
          larIdentifier = lar.larIdentifier.copy(LEI = "B90YWS6AFX2LGWOXJ1LD", id = 2),
          applicant = lar.applicant.copy(
            ethnicity = Ethnicity(
              HispanicOrLatino,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              ethnicityObserved = EthnicityObservedNotApplicable
            ),
            race = Race(
              RaceInformationNotProvided,
              EmptyRaceValue,
              EmptyRaceValue,
              EmptyRaceValue,
              EmptyRaceValue,
              raceObserved = RaceObservedNotApplicable
            ),
            creditScoreType = CreditScoreNotApplicable,
            creditScore = 8888,
            otherCreditScoreModel = "",
            sex = lar.applicant.sex.copy(sexEnum = Male, sexObservedEnum = VisualOrSurnameSex)
          ),
          coApplicant = lar.applicant.copy(
            ethnicity = Ethnicity(
              HispanicOrLatino,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              EmptyEthnicityValue,
              ethnicityObserved = EthnicityObservedNotApplicable
            ),
            race = Race(
              RaceInformationNotProvided,
              EmptyRaceValue,
              EmptyRaceValue,
              EmptyRaceValue,
              EmptyRaceValue,
              raceObserved = RaceObservedNotApplicable
            ),
            creditScoreType = CreditScoreNotApplicable,
            creditScore = 8888,
            otherCreditScoreModel = "",
            sex = lar.applicant.sex.copy(sexEnum = Male, sexObservedEnum = VisualOrSurnameSex)
          ),
          loan = lar.loan.copy(
            ULI = "LESSTHAN22",
            constructionMethod = ManufacturedHome,
            loanPurpose = HomePurchase,
            applicationDate = "NA",
            rateSpread = "NA",
            prepaymentPenaltyTerm = "NA",
            combinedLoanToValueRatio = "NA",
            debtToIncomeRatio = "NA",
            amount = 100000
          ),
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "NA", discountPoints = "NA", totalLoanCosts = "NA"),
          action = lar.action.copy(actionTakenType = PurchasedLoan, preapproval = PreapprovalNotRequested, actionTakenDate = 20190815),
          property = lar.property.copy(
            multiFamilyAffordableUnits = "9",
            manufacturedHomeSecuredProperty = ManufacturedHomeSecuredExempt,
            manufacturedHomeLandPropertyInterest = ManufacturedHomeLoanPropertyInterestExempt,
            totalUnits = 10
          ),
          geography = lar.geography.copy(tract = "na", street = "Exempt", city = "Exempt", zipCode = "Exempt", county = "NA"),
          income = "NA",
          denial = lar.denial.copy(DenialReasonNotApplicable, EmptyDenialValue, EmptyDenialValue, EmptyDenialValue, otherDenialReason = ""),
          lineOfCredit = NotOpenEndLineOfCredit,
          reverseMortgage = NotReverseMortgage,
          businessOrCommercialPurpose = NotPrimarilyBusinessOrCommercialPurpose,
          AUS = lar.AUS.copy(
            aus1 = AUSExempt,
            aus2 = EmptyAUSValue,
            aus3 = EmptyAUSValue,
            aus4 = EmptyAUSValue,
            aus5 = EmptyAUSValue,
            otherAUS = ""
          ),
          ausResult = lar.ausResult.copy(
            ausResult1 = AUSResultExempt,
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = EmptyAUSResultValue,
            ausResult5 = EmptyAUSResultValue,
            otherAusResult = ""
          ),
          payableToInstitution = PayableToInstitutionNotApplicable,
          applicationSubmission = ApplicationSubmissionNotApplicable,
          nonAmortizingFeatures =
            lar.nonAmortizingFeatures.copy(balloonPayment = NoBallonPayment, interestOnlyPayments = NoInterestOnlyPayment),
          hoepaStatus = HOEPStatusANotApplicable
        )
      val testContext = ValidationContext(None, Some(Period(2019, None)))
      val validation  = checkAll(fixedLar, fixedLar.larIdentifier.LEI, testContext, TsValidationError)
      validation.leftMap(errors => errors.toList.size mustBe 0)
    }
  }

  "Lar Validation Engine must capture errors" in {
    forAll(larGen) { lar =>
      eventually {
        val testContext = ValidationContext(None, Some(Period(2019, None)))
        val validation =
          checkAll(lar, lar.larIdentifier.LEI, testContext, TsValidationError)
        val errors =
          validation.leftMap(errors => errors.toList).toEither.left.get
        errors must not be empty
      }
    }
  }
}