package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  DesktopUnderwriter,
  EmptyAUSValue,
  InvalidAutomatedUnderwritingSystemCode,
  InternalProprietarySystem
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V696_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V696_1

  property("AUS fields must be valid") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(
        AUS = lar.AUS.copy(aus1 = DesktopUnderwriter,
                           aus2 = EmptyAUSValue,
                           aus3 = EmptyAUSValue,
                           aus4 = EmptyAUSValue,
                           aus5 = EmptyAUSValue))
      validLar.mustPass

      val invalidLar1 =
        validLar.copy(AUS = validLar.AUS.copy(aus1 = EmptyAUSValue))
      invalidLar1.mustFail

      val invalidLar2 = validLar.copy(
        AUS = validLar.AUS.copy(aus3 = new InvalidAutomatedUnderwritingSystemCode))
      invalidLar2.mustFail

      val invalidLar3 = validLar.copy(
        AUS = validLar.AUS.copy(aus3 = InternalProprietarySystem))
      invalidLar3.mustFail
    }
  }
}
