package hmda.validation.dsl

import java.text.SimpleDateFormat

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._

object PredicateHmda {

  def validDateFormat[T]: Predicate[T] = (_: T) match {
    case s: String =>
      checkDateFormat(s)
    case _ => false
  }

  private def checkDateFormat[T](s: String): Boolean =
    try {
      val format = new SimpleDateFormat("yyyyMMdd")
      format.setLenient(false)
      format.parse(s)
      s.length == 8
    } catch {
      case e: Exception => false
    }

  def exemptionTaken(lar: LoanApplicationRegister): Boolean = {

 lar.applicationSubmission == ApplicationSubmissionExempt ||
 lar.ausResult.ausResult1 == AUSResultExempt ||
 lar.ausResult.ausResult2 == AUSResultExempt || 
 lar.ausResult.ausResult3 == AUSResultExempt || 
 lar.ausResult.ausResult4 == AUSResultExempt || 
 lar.ausResult.ausResult5 == AUSResultExempt ||
 lar.AUS.aus1 == AUSExempt || 
 lar.AUS.aus2 == AUSExempt || 
 lar.AUS.aus3 == AUSExempt || 
 lar.AUS.aus4 == AUSExempt || 
 lar.AUS.aus5 == AUSExempt ||
 lar.nonAmortizingFeatures.balloonPayment == BalloonPaymentExempt ||
 lar.businessOrCommercialPurpose == ExemptBusinessOrCommercialPurpose || 
 lar.applicant.creditScoreType == CreditScoreExempt || 
 lar.coApplicant.creditScoreType == CreditScoreExempt ||
 lar.denial.denialReason1 == ExemptDenialReason ||
 lar.denial.denialReason2 == ExemptDenialReason || 
 lar.denial.denialReason3 == ExemptDenialReason || 
 lar.denial.denialReason4 == ExemptDenialReason ||
 lar.nonAmortizingFeatures.interestOnlyPayments == InterestOnlyPaymentExempt ||
 lar.lineOfCredit == ExemptLineOfCredit || 
 lar.property.manufacturedHomeLandPropertyInterest == ManufacturedHomeLoanPropertyInterestExempt || 
 lar.property.manufacturedHomeSecuredProperty == ManufacturedHomeSecuredExempt ||
 lar.reverseMortgage == ExemptMortgageType || 
 lar.nonAmortizingFeatures.negativeAmortization == NegativeAmortizationExempt || 
 lar.nonAmortizingFeatures.otherNonAmortizingFeatures == OtherNonAmortizingFeaturesExempt || 
 lar.payableToInstitution == PayableToInstitutionExempt
  }
}
