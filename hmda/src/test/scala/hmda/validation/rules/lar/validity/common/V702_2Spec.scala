package scala.hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{EmptyAUSValue, OtherAUS}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity.V702_2

class V702_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V702_2
  property("If no other AUS reported Lar must have at least one AUS as Other") {
    forAll(larGen) { lar =>
      val validAUSLar1 = lar.copy(AUS = lar.AUS.copy(aus1 = OtherAUS))
      val validAUSLar2 = lar.copy(AUS = lar.AUS.copy(aus2 = OtherAUS))
      val validAUSLar3 = lar.copy(AUS = lar.AUS.copy(aus3 = OtherAUS))
      val validAUSLar4 = lar.copy(AUS = lar.AUS.copy(aus4 = OtherAUS))
      val validAUSLar5 = lar.copy(AUS = lar.AUS.copy(aus5 = OtherAUS))
      val invalidAUSLAR = lar.copy(
        AUS = lar.AUS.copy(
          aus1 = EmptyAUSValue,
          aus2 = EmptyAUSValue,
          aus3 = EmptyAUSValue,
          aus4 = EmptyAUSValue,
          aus5 = EmptyAUSValue
        ))

      whenever(lar.AUS.otherAUS != "") {
        validAUSLar1.mustPass
        validAUSLar2.mustPass
        validAUSLar3.mustPass
        validAUSLar4.mustPass
        validAUSLar5.mustPass
        invalidAUSLAR.mustFail
      }

      //Must pass if otherAUS is blank
      invalidAUSLAR.copy(AUS = invalidAUSLAR.AUS.copy(otherAUS = "")).mustPass
    }
  }
}
