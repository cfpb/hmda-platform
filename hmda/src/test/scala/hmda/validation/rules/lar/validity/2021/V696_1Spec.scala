package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.enums._

class V696_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V696_1

  property("2021 AUS Fields must be valid") {
    forAll(larGen) { lar =>
        val validLar = lar.copy(AUS = lar.AUS.copy(
            aus1 = AUSNotApplicable,
            aus2 = EmptyAUSValue,
            aus3 = EmptyAUSValue,
            aus4 = EmptyAUSValue,
            aus5 = EmptyAUSValue
        ))

        validLar.mustPass
        validLar.copy(AUS = validLar.AUS.copy(aus1 = AUSExempt)).mustPass
        validLar.copy(AUS = validLar.AUS.copy(aus1 = InternalProprietarySystem)).mustPass
        validLar.copy(AUS = lar.AUS.copy(aus1 = EmptyAUSValue)).mustFail
        validLar.copy(AUS = lar.AUS.copy(aus2 = AUSExempt)).mustFail
        validLar.copy(AUS = lar.AUS.copy(aus2 = AUSNotApplicable)).mustFail
    }
  }
}