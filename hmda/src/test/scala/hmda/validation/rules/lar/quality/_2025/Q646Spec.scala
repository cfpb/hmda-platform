package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums.{ApplicationSubmissionExempt, _}
import hmda.validation.rules.lar.LarEditCheckSpec

class Q646Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q646

  property("Your file indicates that at least one exemption code was used.") {

    val larList   = larNGen(1).suchThat(_.nonEmpty).sample.getOrElse(Nil)
    val lar = larList(0)



    val larApplicationSubmissionExemptFail =lar.copy(applicationSubmission = ApplicationSubmissionExempt)
    larApplicationSubmissionExemptFail.mustFail
    val larAUSResult1Fail = lar.copy( ausResult = lar.ausResult.copy(ausResult1 = AUSResultExempt))
    larAUSResult1Fail.mustFail
    val larAUSResult2Fail = lar.copy( ausResult = lar.ausResult.copy(ausResult2 = AUSResultExempt))
    larAUSResult2Fail.mustFail
    val larAUSResult3Fail = lar.copy( ausResult = lar.ausResult.copy(ausResult3 = AUSResultExempt))
    larAUSResult3Fail.mustFail
    val larAUSResult4Fail = lar.copy( ausResult = lar.ausResult.copy(ausResult4 = AUSResultExempt))
    larAUSResult4Fail.mustFail
    val larAUSResult5Fail = lar.copy( ausResult = lar.ausResult.copy(ausResult5 = AUSResultExempt))
    larAUSResult5Fail.mustFail


    val larListAlt   = larNGen(1).suchThat(_.nonEmpty).sample.getOrElse(Nil)
    val larAlt = larListAlt(0)
    
    val larAUS1Fail = larAlt.copy( AUS = larAlt.AUS.copy(aus1 = AUSExempt))
    larAUS1Fail.mustFail
    val larAUS2Fail = larAlt.copy( AUS = larAlt.AUS.copy(aus2 = AUSExempt))
    larAUS2Fail.mustFail
    val larAUS3Fail = larAlt.copy( AUS = larAlt.AUS.copy(aus3 = AUSExempt))
    larAUS3Fail.mustFail
    val larAUS4Fail = larAlt.copy( AUS = larAlt.AUS.copy(aus4 = AUSExempt))
    larAUS4Fail.mustFail
    val larAUS5Fail = larAlt.copy( AUS = larAlt.AUS.copy(aus5 = AUSExempt))
    larAUS5Fail.mustFail

    val larBalloonFail = larAlt.copy( nonAmortizingFeatures = larAlt.nonAmortizingFeatures.copy(balloonPayment = BalloonPaymentExempt))
    larBalloonFail.mustFail

    val larBusinessOrCommercialPurposeFail = larAlt.copy( businessOrCommercialPurpose = ExemptBusinessOrCommercialPurpose)
    larBusinessOrCommercialPurposeFail.mustFail

    val larCreditScoreTypeFail = larAlt.copy( applicant = larAlt.applicant.copy(creditScoreType = CreditScoreExempt))
    larCreditScoreTypeFail.mustFail

    val larCoCreditScoreTypeFail = larAlt.copy( coApplicant = larAlt.coApplicant.copy(creditScoreType = CreditScoreExempt))
    larCoCreditScoreTypeFail.mustFail


    val lardenialReason1Fail = larAlt.copy( denial = larAlt.denial.copy(denialReason1 = ExemptDenialReason))
    lardenialReason1Fail.mustFail
    val lardenialReason2Fail = larAlt.copy( denial = larAlt.denial.copy(denialReason2 = ExemptDenialReason))
    lardenialReason2Fail.mustFail
    val lardenialReason3Fail = larAlt.copy( denial = larAlt.denial.copy(denialReason3 = ExemptDenialReason))
    lardenialReason3Fail.mustFail
    val lardenialReason4Fail = larAlt.copy( denial = larAlt.denial.copy(denialReason4 = ExemptDenialReason))
    lardenialReason4Fail.mustFail

    val larInterestOnlyPaymentsFail = larAlt.copy( nonAmortizingFeatures = larAlt.nonAmortizingFeatures.copy(interestOnlyPayments = InterestOnlyPaymentExempt))
    larInterestOnlyPaymentsFail.mustFail


    val larNegativeAmortizationExemptFail = larAlt.copy( nonAmortizingFeatures = larAlt.nonAmortizingFeatures.copy(negativeAmortization = NegativeAmortizationExempt))
    larNegativeAmortizationExemptFail.mustFail

    val larOtherNonAmortizingFeaturesExemptFail = larAlt.copy( nonAmortizingFeatures = larAlt.nonAmortizingFeatures.copy(otherNonAmortizingFeatures = OtherNonAmortizingFeaturesExempt))
    larOtherNonAmortizingFeaturesExemptFail.mustFail

    val larPayableToInstitutionExemptFail =lar.copy(payableToInstitution = PayableToInstitutionExempt)
    larPayableToInstitutionExemptFail.mustFail

    val larExemptLineOfCreditFail =lar.copy(lineOfCredit = ExemptLineOfCredit)
    larExemptLineOfCreditFail.mustFail


    val larExemptMortgageTypeFail =lar.copy(reverseMortgage = ExemptMortgageType)
    larExemptMortgageTypeFail.mustFail


    val larManufacturedInterestExemptFail = larAlt.copy( property = larAlt.property.copy(manufacturedHomeLandPropertyInterest = ManufacturedHomeLoanPropertyInterestExempt))
    larManufacturedInterestExemptFail.mustFail

    val larSecuredPropertyFail = larAlt.copy( property = larAlt.property.copy(manufacturedHomeSecuredProperty = ManufacturedHomeSecuredExempt))
    larSecuredPropertyFail.mustFail


  }
}