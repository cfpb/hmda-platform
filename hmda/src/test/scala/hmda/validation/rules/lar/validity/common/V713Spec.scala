package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V713Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V713

  property("When AUS exemption is taken, all AUS fields must be exempt") {
    forAll(larGen) { lar =>

      val appLar = lar.copy(AUS = lar.AUS.copy(aus1 = AUSExempt),
                            ausResult =
                              lar.ausResult.copy(ausResult1 = AUSResultExempt))

      appLar
        .copy(ausResult = appLar.ausResult.copy(otherAusResult = "test"))
        .mustFail
      appLar.copy(AUS = appLar.AUS.copy(otherAUS = "test")).mustFail

      appLar
        .copy(ausResult = appLar.ausResult.copy(ausResult2 = ApproveEligible))
        .mustFail
      appLar.copy(AUS = appLar.AUS.copy(aus3 = LoanProspector)).mustFail

      val validAUS = appLar.AUS.copy(aus2 = EmptyAUSValue,
                                     aus3 = EmptyAUSValue,
                                     aus4 = EmptyAUSValue,
                                     aus5 = EmptyAUSValue,
                                     otherAUS = "")
      val valAusResult = appLar.ausResult.copy(ausResult2 = EmptyAUSResultValue,
                                               ausResult3 = EmptyAUSResultValue,
                                               ausResult4 = EmptyAUSResultValue,
                                               ausResult5 = EmptyAUSResultValue,
                                               otherAusResult = "")

      appLar.copy(AUS = validAUS, ausResult = valAusResult).mustPass
    }
  }
}
