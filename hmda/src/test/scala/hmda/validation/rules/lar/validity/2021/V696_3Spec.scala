package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.enums._

class V696_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V696_3

  property("2021 AUS Result Fields must be valid") {
    forAll(larGen) { lar =>
        val aus2EmptyLar = lar.copy(AUS = lar.AUS.copy(
            aus2 = EmptyAUSValue,
            aus3 = DesktopUnderwriter,
            aus4 = DesktopUnderwriter,
            aus5 = DesktopUnderwriter
        ))

        aus2EmptyLar.copy(ausResult = aus2EmptyLar.ausResult.copy(
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = Ineligible,
            ausResult4 = Ineligible,
            ausResult5 = Ineligible
        )).mustPass

        aus2EmptyLar.copy(ausResult = aus2EmptyLar.ausResult.copy(
            ausResult2 = Ineligible,
            ausResult3 = Ineligible,
            ausResult4 = Ineligible,
            ausResult5 = Ineligible
        )).mustFail

        aus2EmptyLar.copy(ausResult = aus2EmptyLar.ausResult.copy(
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = Ineligible,
            ausResult5 = Ineligible
        )).mustFail

    }
  }
}