package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.enums._

class V696_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V696_2

  property("2021 AUS Result Fields must be valid") {
    forAll(larGen) { lar =>
        val validLar = lar.copy(ausResult = lar.ausResult.copy(
            ausResult1 = AutomatedUnderwritingResultNotApplicable,
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = EmptyAUSResultValue,
            ausResult5 = EmptyAUSResultValue
        ))

        validLar.mustPass
        validLar.copy(ausResult = validLar.ausResult.copy(ausResult1 = AUSResultExempt)).mustPass

        lar.copy(ausResult = lar.ausResult.copy(ausResult1 = EmptyAUSResultValue)).mustFail
        lar.copy(ausResult = lar.ausResult.copy(ausResult2 = AUSResultExempt)).mustFail
        lar.copy(ausResult = lar.ausResult.copy(ausResult2 = AutomatedUnderwritingResultNotApplicable)).mustFail
    }
  }
}