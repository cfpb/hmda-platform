package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q643Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q643

  property("[2019] AUS Result should be valid for the corresponding AUS") {
    forAll(larGen) { lar =>
      lar
        .copy(
          AUS = lar.AUS.copy(aus1 = EmptyAUSValue,
                             aus2 = EmptyAUSValue,
                             aus3 = EmptyAUSValue,
                             aus4 = EmptyAUSValue,
                             aus5 = EmptyAUSValue))
        .mustPass

      val appLar = lar.copy(
        AUS = lar.AUS.copy(aus1 = EmptyAUSValue,
                           aus2 = DesktopUnderwriter,
                           aus3 = EmptyAUSValue,
                           aus4 = DesktopUnderwriter,
                           aus5 = EmptyAUSValue))

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(
            ausResult1 = EmptyAUSResultValue,
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = EmptyAUSResultValue,
            ausResult5 = EmptyAUSResultValue
          ))
        .mustFail

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(
            ausResult1 = EmptyAUSResultValue,
            ausResult2 = ApproveEligible,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = OutOfScope,
            ausResult5 = EmptyAUSResultValue
          ))
        .mustPass

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(
            ausResult1 = EmptyAUSResultValue,
            ausResult2 = OtherAutomatedUnderwritingResult,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = OutOfScope,
            ausResult5 = EmptyAUSResultValue
          ))
        .mustPass
    }
  }
}
