package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V700_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V700_1

  property("When AUS is NA, AUS Result must be NA") {
    forAll(larGen) { lar =>
      val relevantLar = lar.copy(AUS = lar.AUS.copy(aus1 = AUSNotApplicable))

      val irrelevantLar =
        lar.copy(AUS = lar.AUS.copy(aus1 = DesktopUnderwriter))
      irrelevantLar.mustPass

      val emptyAUS = relevantLar.AUS.copy(
        aus2 = EmptyAUSValue,
        aus3 = EmptyAUSValue,
        aus4 = EmptyAUSValue,
        aus5 = EmptyAUSValue
      )

      val emptyAusResult = relevantLar.ausResult.copy(
        ausResult2 = EmptyAUSResultValue,
        ausResult3 = EmptyAUSResultValue,
        ausResult4 = EmptyAUSResultValue,
        ausResult5 = EmptyAUSResultValue
      )

      val testLar = relevantLar.copy(AUS = emptyAUS, ausResult = emptyAusResult)

      val validLar = testLar.copy(
        ausResult = emptyAusResult.copy(
          ausResult1 = AutomatedUnderwritingResultNotApplicable))
      validLar.mustPass
      validLar.copy(AUS = validLar.AUS.copy(aus2 = AUSNotApplicable)).mustFail
      validLar
        .copy(
          ausResult = validLar.ausResult.copy(
            ausResult2 = AutomatedUnderwritingResultNotApplicable))
        .mustFail
      testLar
        .copy(ausResult = emptyAusResult.copy(ausResult1 = ApproveEligible))
        .mustFail
    }
  }
}
