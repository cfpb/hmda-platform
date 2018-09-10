package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{
  ApproveEligible,
  EmptyAUSResultValue,
  EmptyAUSValue
}

class V701Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V701

  property("When AUS is Blank AUS Result must be blank") {
    forAll(larGen) { lar =>
      val relevantLar = lar.copy(
        AUS = lar.AUS.copy(
          aus2 = EmptyAUSValue,
          aus3 = EmptyAUSValue,
          aus4 = EmptyAUSValue,
          aus5 = EmptyAUSValue
        ))
      val validAUSResult = lar.ausResult.copy(
        ausResult2 = EmptyAUSResultValue,
        ausResult3 = EmptyAUSResultValue,
        ausResult4 = EmptyAUSResultValue,
        ausResult5 = EmptyAUSResultValue
      )
      val invalidAUSResult1 = lar.ausResult.copy(ausResult2 = ApproveEligible)
      val invalidAUSResult2 = lar.ausResult.copy(ausResult3 = ApproveEligible)
      val invalidAUSResult3 = lar.ausResult.copy(ausResult4 = ApproveEligible)
      val invalidAUSResult4 = lar.ausResult.copy(ausResult5 = ApproveEligible)

      relevantLar.copy(ausResult = validAUSResult).mustPass
      relevantLar.copy(ausResult = invalidAUSResult1).mustFail
      relevantLar.copy(ausResult = invalidAUSResult2).mustFail
      relevantLar.copy(ausResult = invalidAUSResult3).mustFail
      relevantLar.copy(ausResult = invalidAUSResult4).mustFail
    }
  }
}
