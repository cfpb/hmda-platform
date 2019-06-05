package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V696_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V696_3

  property("Number of reported AUS and AUS result fields must be equal") {
    forAll(larGen) { lar =>
      val aus2reported = lar.AUS.copy(aus1 = DesktopUnderwriter,
                                      aus2 = EmptyAUSValue,
                                      aus3 = EmptyAUSValue,
                                      aus4 = DesktopUnderwriter,
                                      aus5 = EmptyAUSValue)

      val aus3reported = aus2reported.copy(aus2 = DesktopUnderwriter)

      val ausResult3Reported =
        lar.ausResult.copy(ausResult1 = ApproveEligible,
                           ausResult2 = EmptyAUSResultValue,
                           ausResult3 = ApproveEligible,
                           ausResult4 = EmptyAUSResultValue,
                           ausResult5 = ApproveEligible)

      val validLar =
        lar.copy(AUS = aus3reported, ausResult = ausResult3Reported)
      validLar.mustPass

      val invalidLar = validLar.copy(AUS = aus2reported)
      invalidLar.mustFail
    }
  }
}
