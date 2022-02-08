package hmda.parser.filing.lar

import cats.implicits._
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar._
import hmda.model.filing.lar.enums._
import hmda.parser.LarParserValidationResult
import hmda.parser.filing.lar.ApplicantFormatValidator._
import hmda.parser.filing.lar.LarParserErrorModel._


sealed trait LarFormatValidator extends LarParser {

  val config = ConfigFactory.load()

  val currentYear    = config.getString("hmda.filing.current")
  val numberOfFields = config.getInt(s"hmda.filing.$currentYear.lar.length")

  def validateLar(
                   values: Seq[String],
                   rawLine: String = "",
                   fromCassandra: Boolean = false
                 ): LarParserValidationResult[LoanApplicationRegister] = {

    if (values.lengthCompare(numberOfFields) != 0 || (rawLine.trim.endsWith("|") && (!fromCassandra))) {
      IncorrectNumberOfFieldsLar(values.length.toString).invalidNel
    } else {
      val id                                   = values.headOption.getOrElse("")
      val lei                                  = values(1).toUpperCase
      val uli                                  = values(2)
      val applicationDate                      = values(3)
      val loanType                             = values(4)
      val loanPurpose                          = values(5)
      val preapproval                          = values(6)
      val constructionMethod                   = values(7)
      val occupancy                            = values(8)
      val loanAmount                           = values(9)
      val actionTaken                          = values(10)
      val actionTakenDate                      = values(11)
      val street                               = values(12)
      val city                                 = values(13)
      val state                                = values(14)
      val zipCode                              = values(15)
      val county                               = values(16)
      val tract                                = values(17)
      val appEth1                              = values(18)
      val appEth2                              = values(19)
      val appEth3                              = values(20)
      val appEth4                              = values(21)
      val appEth5                              = values(22)
      val appEthOther                          = values(23)
      val coAppEth1                            = values(24)
      val coAppEth2                            = values(25)
      val coAppEth3                            = values(26)
      val coAppEth4                            = values(27)
      val coAppEth5                            = values(28)
      val coAppEthOther                        = values(29)
      val appEthObserved                       = values(30)
      val coAppEthObserved                     = values(31)
      val appRace1                             = values(32)
      val appRace2                             = values(33)
      val appRace3                             = values(34)
      val appRace4                             = values(35)
      val appRace5                             = values(36)
      val appOtherNative                       = values(37)
      val appOtherAsian                        = values(38)
      val appOtherPacific                      = values(39)
      val coAppRace1                           = values(40)
      val coAppRace2                           = values(41)
      val coAppRace3                           = values(42)
      val coAppRace4                           = values(43)
      val coAppRace5                           = values(44)
      val coAppOtherNative                     = values(45)
      val coAppOtherAsian                      = values(46)
      val coAppOtherPacific                    = values(47)
      val appRaceObserved                      = values(48)
      val coAppRaceObserved                    = values(49)
      val appSex                               = values(50)
      val coAppSex                             = values(51)
      val appSexObserved                       = values(52)
      val coAppSexObserved                     = values(53)
      val appAge                               = values(54)
      val coAppAge                             = values(55)
      val income                               = values(56)
      val purchaserType                        = values(57)
      val rateSpread                           = values(58)
      val hoepaStatus                          = values(59)
      val lienStatus                           = values(60)
      val appCreditScore                       = values(61)
      val coAppCreditScore                     = values(62)
      val appCreditScoreModel                  = values(63)
      val appCreditScoreModelOther             = values(64)
      val coAppCreditScoreModel                = values(65)
      val coAppCreditScoreModelOther           = values(66)
      val denial1                              = values(67)
      val denial2                              = values(68)
      val denial3                              = values(69)
      val denial4                              = values(70)
      val denialOther                          = values(71)
      val totalLoanCosts                       = values(72)
      val totalPointsAndFees                   = values(73)
      val originationCharges                   = values(74)
      val discountPoints                       = values(75)
      val lenderCredits                        = values(76)
      val interestRate                         = values(77)
      val prepaymentPenaltyTerm                = values(78)
      val debtToIncomeRatio                    = values(79)
      val loanToValueRatio                     = values(80)
      val loanTerm                             = values(81)
      val introductoryRatePeriod               = values(82)
      val balloonPayment                       = values(83)
      val interestOnlyPayment                  = values(84)
      val negativeAmortization                 = values(85)
      val otherNonAmortizingFeatures           = values(86)
      val propertyValue                        = values(87)
      val manufacturedHomeSecuredProperty      = values(88)
      val manufacturedHomeLandPropertyInterest = values(89)
      val totalUnits                           = values(90)
      val multifamilyAffordableUnits           = values(91)
      val submissionOfApplication              = values(92)
      val payableToInstitution                 = values(93)
      val nmlsrIdentifier                      = values(94)
      val aus1                                 = values(95)
      val aus2                                 = values(96)
      val aus3                                 = values(97)
      val aus4                                 = values(98)
      val aus5                                 = values(99)
      val ausOther                             = values(100)
      val ausResult1                           = values(101)
      val ausResult2                           = values(102)
      val ausResult3                           = values(103)
      val ausResult4                           = values(104)
      val ausResult5                           = values(105)
      val ausResultOther                       = values(106)
      val reverseMortgate                      = values(107)
      val openEndLineOfCredit                  = values(108)
      val businessOrCommercial                 = values(109)

      validateLarValues(
        id,
        lei,
        uli,
        applicationDate,
        loanType,
        loanPurpose,
        preapproval,
        constructionMethod,
        occupancy,
        loanAmount,
        actionTaken,
        actionTakenDate,
        street,
        city,
        state,
        zipCode,
        county,
        tract,
        appEth1,
        appEth2,
        appEth3,
        appEth4,
        appEth5,
        appEthOther,
        coAppEth1,
        coAppEth2,
        coAppEth3,
        coAppEth4,
        coAppEth5,
        coAppEthOther,
        appEthObserved,
        coAppEthObserved,
        appRace1,
        appRace2,
        appRace3,
        appRace4,
        appRace5,
        appOtherNative,
        appOtherAsian,
        appOtherPacific,
        coAppRace1,
        coAppRace2,
        coAppRace3,
        coAppRace4,
        coAppRace5,
        coAppOtherNative,
        coAppOtherAsian,
        coAppOtherPacific,
        appRaceObserved,
        coAppRaceObserved,
        appSex,
        coAppSex,
        appSexObserved,
        coAppSexObserved,
        appAge,
        coAppAge,
        income,
        purchaserType,
        rateSpread,
        hoepaStatus,
        lienStatus,
        appCreditScore,
        coAppCreditScore,
        appCreditScoreModel,
        appCreditScoreModelOther,
        coAppCreditScoreModel,
        coAppCreditScoreModelOther,
        denial1,
        denial2,
        denial3,
        denial4,
        denialOther,
        totalLoanCosts,
        totalPointsAndFees,
        originationCharges,
        discountPoints,
        lenderCredits,
        interestRate,
        prepaymentPenaltyTerm,
        debtToIncomeRatio,
        loanToValueRatio,
        loanTerm,
        introductoryRatePeriod,
        balloonPayment,
        interestOnlyPayment,
        negativeAmortization,
        otherNonAmortizingFeatures,
        propertyValue,
        manufacturedHomeSecuredProperty,
        manufacturedHomeLandPropertyInterest,
        totalUnits,
        multifamilyAffordableUnits,
        submissionOfApplication,
        payableToInstitution,
        nmlsrIdentifier,
        aus1,
        aus2,
        aus3,
        aus4,
        aus5,
        ausOther,
        ausResult1,
        ausResult2,
        ausResult3,
        ausResult4,
        ausResult5,
        ausResultOther,
        reverseMortgate,
        openEndLineOfCredit,
        businessOrCommercial
      )
    }

  }

  def validateLarValues(
                         id: String,
                         lei: String,
                         uli: String,
                         applicationDate: String,
                         loanType: String,
                         loanPurpose: String,
                         preapproval: String,
                         constructionMethod: String,
                         occupancy: String,
                         loanAmount: String,
                         actionTaken: String,
                         actionTakenDate: String,
                         street: String,
                         city: String,
                         state: String,
                         zipCode: String,
                         county: String,
                         tract: String,
                         appEth1: String,
                         appEth2: String,
                         appEth3: String,
                         appEth4: String,
                         appEth5: String,
                         appEthOther: String,
                         coAppEth1: String,
                         coAppEth2: String,
                         coAppEth3: String,
                         coAppEth4: String,
                         coAppEth5: String,
                         coAppEthOther: String,
                         appEthObserved: String,
                         coAppEthObserved: String,
                         appRace1: String,
                         appRace2: String,
                         appRace3: String,
                         appRace4: String,
                         appRace5: String,
                         appOtherNative: String,
                         appOtherAsian: String,
                         appOtherPacific: String,
                         coAppRace1: String,
                         coAppRace2: String,
                         coAppRace3: String,
                         coAppRace4: String,
                         coAppRace5: String,
                         coAppOtherNative: String,
                         coAppOtherAsian: String,
                         coAppOtherPacific: String,
                         appRaceObserved: String,
                         coAppRaceObserved: String,
                         appSex: String,
                         coAppSex: String,
                         appSexObserved: String,
                         coAppSexObserved: String,
                         appAge: String,
                         coAppAge: String,
                         income: String,
                         purchaserType: String,
                         rateSpread: String,
                         hoepaStatus: String,
                         lienStatus: String,
                         appCreditScore: String,
                         coAppCreditScore: String,
                         appCreditScoreModel: String,
                         appCreditScoreModelOther: String,
                         coAppCreditScoreModel: String,
                         coAppCreditScoreModelOther: String,
                         denial1: String,
                         denial2: String,
                         denial3: String,
                         denial4: String,
                         denialOther: String,
                         totalLoanCosts: String,
                         totalPointsAndFees: String,
                         originationCharges: String,
                         discountPoints: String,
                         lenderCredits: String,
                         interestRate: String,
                         prepaymentPenaltyTerm: String,
                         debtToIncomeRatio: String,
                         loanToValueRatio: String,
                         loanTerm: String,
                         introductoryRatePeriod: String,
                         balloonPayment: String,
                         interestOnlyPayment: String,
                         negativeAmortization: String,
                         otherNonAmortizingFeatures: String,
                         propertyValue: String,
                         manufacturedHomeSecuredProperty: String,
                         manufacturedHomeLandPropertyInterest: String,
                         totalUnits: String,
                         multifamilyAffordableUnits: String,
                         submissionOfApplication: String,
                         payableToInstitution: String,
                         nmlsrIdentifier: String,
                         aus1: String,
                         aus2: String,
                         aus3: String,
                         aus4: String,
                         aus5: String,
                         ausOther: String,
                         ausResult1: String,
                         ausResult2: String,
                         ausResult3: String,
                         ausResult4: String,
                         ausResult5: String,
                         ausResultOther: String,
                         reverseMortgage: String,
                         openEndLineOfCredit: String,
                         businessOrCommercial: String
                       ): LarParserValidationResult[LoanApplicationRegister] =
    (
      validateLarIdentifier(id, lei, nmlsrIdentifier),
      validateLoan(
        uli,
        applicationDate,
        loanType,
        loanPurpose,
        constructionMethod,
        occupancy,
        loanAmount,
        loanTerm,
        rateSpread,
        interestRate,
        prepaymentPenaltyTerm,
        debtToIncomeRatio,
        loanToValueRatio,
        introductoryRatePeriod
      ),
      validateLarAction(preapproval, actionTaken, actionTakenDate),
      validateGeography(street, city, state, zipCode, county, tract),
      validateApplicant(
        appEth1,
        appEth2,
        appEth3,
        appEth4,
        appEth5,
        appEthOther,
        appEthObserved,
        appRace1,
        appRace2,
        appRace3,
        appRace4,
        appRace5,
        appOtherNative,
        appOtherAsian,
        appOtherPacific,
        appRaceObserved,
        appSex,
        appSexObserved,
        appAge,
        appCreditScore,
        appCreditScoreModel,
        appCreditScoreModelOther,
        coApp = false
      ),
      validateApplicant(
        coAppEth1,
        coAppEth2,
        coAppEth3,
        coAppEth4,
        coAppEth5,
        coAppEthOther,
        coAppEthObserved,
        coAppRace1,
        coAppRace2,
        coAppRace3,
        coAppRace4,
        coAppRace5,
        coAppOtherNative,
        coAppOtherAsian,
        coAppOtherPacific,
        coAppRaceObserved,
        coAppSex,
        coAppSexObserved,
        coAppAge,
        coAppCreditScore,
        coAppCreditScoreModel,
        coAppCreditScoreModelOther,
        coApp = true
      ),
      validateIntStrOrNAField(income, InvalidIncome(income)),
      validateLarCode(PurchaserEnum, purchaserType, InvalidPurchaserType(purchaserType)),
      validateLarCode(HOEPAStatusEnum, hoepaStatus, InvalidHoepaStatus(hoepaStatus)),
      validateLarCode(LienStatusEnum, lienStatus, InvalidLienStatus(lienStatus)),
      validateDenial(denial1, denial2, denial3, denial4, denialOther),
      validateLoanDisclosure(totalLoanCosts, totalPointsAndFees, originationCharges, discountPoints, lenderCredits),
      validateNonAmortizingFeatures(balloonPayment, interestOnlyPayment, negativeAmortization, otherNonAmortizingFeatures),
      validateProperty(
        propertyValue,
        manufacturedHomeSecuredProperty,
        manufacturedHomeLandPropertyInterest,
        totalUnits,
        multifamilyAffordableUnits
      ),
      validateLarCode(ApplicationSubmissionEnum, submissionOfApplication, InvalidApplicationSubmission(submissionOfApplication)),
      validateLarCode(PayableToInstitutionEnum, payableToInstitution, InvalidPayableToInstitution(payableToInstitution)),
      validateAus(aus1, aus2, aus3, aus4, aus5, ausOther),
      validateAusResult(ausResult1, ausResult2, ausResult3, ausResult4, ausResult5, ausResultOther),
      validateLarCode(MortgageTypeEnum, reverseMortgage, InvalidMortgageType(reverseMortgage)),
      validateLarCode(LineOfCreditEnum, openEndLineOfCredit, InvalidLineOfCredit(openEndLineOfCredit)),
      validateLarCode(BusinessOrCommercialBusinessEnum, businessOrCommercial, InvalidBusinessOrCommercial(businessOrCommercial))
      ).mapN(LoanApplicationRegister.apply)

  def validateLarIdentifier(
                             id: String,
                             LEI: String,
                             NMLSRIdentifier: String
                           ): LarParserValidationResult[LarIdentifier] =
    (
      validateIntField(id, InvalidLarId(id)),
      validateStrNoSpace(LEI, InvalidLei(LEI)),
      validateStr(NMLSRIdentifier)
      ).mapN(LarIdentifier.apply)

  def validateLoan(
                    uli: String,
                    applicationDate: String,
                    loanType: String,
                    loanPurpose: String,
                    constructionMethod: String,
                    occupancy: String,
                    amount: String,
                    loanTerm: String,
                    rateSpread: String,
                    interestRate: String,
                    prepaymentPenalty: String,
                    debtToIncome: String,
                    loanToValue: String,
                    introductoryRate: String
                  ): LarParserValidationResult[Loan] =
    (
      validateStrNoSpace(uli, InvalidULI(uli)),
      validateDateOrNaField(applicationDate, InvalidApplicationDate(applicationDate)),
      validateLarCode(LoanTypeEnum, loanType, InvalidLoanType(loanType)),
      validateLarCode(LoanPurposeEnum, loanPurpose, InvalidLoanPurpose(loanPurpose)),
      validateLarCode(ConstructionMethodEnum, constructionMethod, InvalidConstructionMethod(constructionMethod)),
      validateLarCode(OccupancyEnum, occupancy, InvalidOccupancy(occupancy)),
      validateBigDecimalField(amount, InvalidAmount(amount)),
      validateIntStrOrNAOrExemptField(loanTerm, InvalidLoanTerm(loanTerm)),
      validateDoubleStrOrNAOrExemptField(rateSpread, InvalidRateSpread(rateSpread)),
      validateDoubleStrOrNAOrExemptField(interestRate, InvalidInterestRate(interestRate)),
      validateIntStrOrNAOrExemptField(prepaymentPenalty, InvalidPrepaymentPenaltyTerm(prepaymentPenalty)),
      validateDoubleStrOrNAOrExemptField(debtToIncome, InvalidDebtToIncomeRatio(debtToIncome)),
      validateDoubleStrOrNAOrExemptField(loanToValue, InvalidLoanToValueRatio(loanToValue)),
      validateIntStrOrNAOrExemptField(introductoryRate, InvalidIntroductoryRatePeriod(introductoryRate))
      ).mapN(Loan.apply)

  def validateLarAction(preapproval: String, actionTaken: String, actionDate: String): LarParserValidationResult[LarAction] =
    (
      validateLarCode(PreapprovalEnum, preapproval, InvalidPreapproval(preapproval)),
      validateLarCode(ActionTakenTypeEnum, actionTaken, InvalidActionTaken(actionTaken)),
      validateDateField(actionDate, InvalidActionTakenDate(actionDate))
      ).mapN(LarAction.apply)

  def validateGeography(
                         street: String,
                         city: String,
                         state: String,
                         zipCode: String,
                         county: String,
                         tract: String
                       ): LarParserValidationResult[Geography] =
    (
      validateStrOrNAOrExemptField(street,InvalidStreet(street)),
      validateStrOrNAOrExemptField(city,InvalidCity(city)),
      validateStrOrNAOrExemptField(state,InvalidStreet(state)),
      validateStrOrNAOrExemptField(zipCode,InvalidZipcode(zipCode)),
      validateStr(county),
      validateStr(tract)
      ).mapN(Geography.apply)

  def validateDenial(
                      denial1: String,
                      denial2: String,
                      denial3: String,
                      denial4: String,
                      otherDenial: String
                    ): LarParserValidationResult[Denial] =
    (
      validateLarCode(DenialReasonEnum, denial1, InvalidDenial(1, denial1)),
      validateLarCodeOrEmptyField(DenialReasonEnum, denial2, InvalidDenial(2, denial2)),
      validateLarCodeOrEmptyField(DenialReasonEnum, denial3, InvalidDenial(3, denial3)),
      validateLarCodeOrEmptyField(DenialReasonEnum, denial4, InvalidDenial(4, denial4)),
      validateStr(otherDenial)
      ).mapN(Denial.apply)

  def validateLoanDisclosure(
                              totalLoanCosts: String,
                              totalPointsAndFees: String,
                              originationCharges: String,
                              discountPoints: String,
                              lenderCredits: String
                            ): LarParserValidationResult[LoanDisclosure] =
    (
      validateDoubleStrOrNAOrExemptField(totalLoanCosts, InvalidTotalLoanCosts(totalLoanCosts)),
      validateDoubleStrOrNAOrExemptField(totalPointsAndFees, InvalidPointsAndFees(totalPointsAndFees)),
      validateDoubleStrOrNAOrExemptField(originationCharges, InvalidOriginationCharges(originationCharges)),
      validateDoubleStrOrNAOrExemptOrEmptyField(discountPoints, InvalidDiscountPoints(discountPoints)),
      validateDoubleStrOrNAOrExemptOrEmptyField(lenderCredits, InvalidLenderCredits(lenderCredits))
      ).mapN(LoanDisclosure.apply)

  def validateNonAmortizingFeatures(
                                     ballonPayment: String,
                                     interestOnlyPayment: String,
                                     negativeAmortization: String,
                                     otherNonAmortizingFeatures: String
                                   ): LarParserValidationResult[NonAmortizingFeatures] =
    (
      validateLarCode(BalloonPaymentEnum, ballonPayment, InvalidBalloonPayment(ballonPayment)),
      validateLarCode(InterestOnlyPaymentsEnum, interestOnlyPayment, InvalidInterestOnlyPayment(interestOnlyPayment)),
      validateLarCode(NegativeAmortizationEnum, negativeAmortization, InvalidNegativeAmortization(negativeAmortization)),
      validateLarCode(
        OtherNonAmortizingFeaturesEnum,
        otherNonAmortizingFeatures,
        InvalidOtherNonAmortizingFeatures(otherNonAmortizingFeatures)
      )
      ).mapN(NonAmortizingFeatures.apply)

  def validateProperty(
                        propertyValue: String,
                        manufacturedHomeSecuredProperty: String,
                        manufacturedHomeLandPropertyInterest: String,
                        totalUnits: String,
                        multifamilyUnits: String
                      ): LarParserValidationResult[Property] =
    (
      validateDoubleStrOrNAOrExemptField(propertyValue, InvalidPropertyValue(propertyValue)),
      validateLarCode(
        ManufacturedHomeSecuredPropertyEnum,
        manufacturedHomeSecuredProperty,
        InvalidManufacturedHomeSecuredProperty(manufacturedHomeSecuredProperty)
      ),
      validateLarCode(
        ManufacturedHomeLandPropertyInterestEnum,
        manufacturedHomeLandPropertyInterest,
        InvalidManufacturedHomeLandPropertyInterest(manufacturedHomeLandPropertyInterest)
      ),
      validateIntField(totalUnits, InvalidTotalUnits(totalUnits)),
      validateIntStrOrNAOrExemptField(multifamilyUnits, InvalidMultifamilyUnits(multifamilyUnits))
      ).mapN(Property.apply)

  def validateAus(
                   aus1: String,
                   aus2: String,
                   aus3: String,
                   aus4: String,
                   aus5: String,
                   otherAus: String
                 ): LarParserValidationResult[AutomatedUnderwritingSystem] =
    (
      validateLarCode(AutomatedUnderwritingSystemEnum, aus1, InvalidAutomatedUnderwritingSystem(1, aus1)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingSystemEnum, aus2, InvalidAutomatedUnderwritingSystem(2, aus2)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingSystemEnum, aus3, InvalidAutomatedUnderwritingSystem(3, aus3)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingSystemEnum, aus4, InvalidAutomatedUnderwritingSystem(4, aus4)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingSystemEnum, aus5, InvalidAutomatedUnderwritingSystem(5, aus5)),
      validateStr(otherAus)
      ).mapN(AutomatedUnderwritingSystem.apply)

  def validateAusResult(
                         ausResult1: String,
                         ausResult2: String,
                         ausResult3: String,
                         ausResult4: String,
                         ausResult5: String,
                         otherAusResult: String
                       ): LarParserValidationResult[AutomatedUnderwritingSystemResult] =
    (
      validateLarCode(AutomatedUnderwritingResultEnum, ausResult1, InvalidAutomatedUnderwritingSystemResult(1, ausResult1)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, ausResult2, InvalidAutomatedUnderwritingSystemResult(2, ausResult2)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, ausResult3, InvalidAutomatedUnderwritingSystemResult(3, ausResult3)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, ausResult4, InvalidAutomatedUnderwritingSystemResult(4, ausResult4)),
      validateLarCodeOrEmptyField(AutomatedUnderwritingResultEnum, ausResult5, InvalidAutomatedUnderwritingSystemResult(5, ausResult5)),
      validateStr(otherAusResult)
      ).mapN(AutomatedUnderwritingSystemResult.apply)

}

object LarFormatValidator extends LarFormatValidator