package scala.hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  EmptyAUSResultValue,
  OtherAutomatedUnderwritingResult
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity.V703_2

class V703_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V703_2
  property("If no other AUS reported Lar must have at least one AUS as Other") {
    forAll(larGen) { lar =>
      val validAUSResultLar1 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult1 = OtherAutomatedUnderwritingResult))
      val validAUSResultLar2 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult2 = OtherAutomatedUnderwritingResult))
      val validAUSResultLar3 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult3 = OtherAutomatedUnderwritingResult))
      val validAUSResultLar4 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult4 = OtherAutomatedUnderwritingResult))
      val validAUSResultLar5 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult5 = OtherAutomatedUnderwritingResult))
      val invalidAUSResultLAR = lar.copy(
        ausResult = lar.ausResult.copy(
          ausResult1 = EmptyAUSResultValue,
          ausResult2 = EmptyAUSResultValue,
          ausResult3 = EmptyAUSResultValue,
          ausResult4 = EmptyAUSResultValue,
          ausResult5 = EmptyAUSResultValue
        ))
      whenever(lar.ausResult.otherAusResult != "") {
        validAUSResultLar1.mustPass
        validAUSResultLar2.mustPass
        validAUSResultLar3.mustPass
        validAUSResultLar4.mustPass
        validAUSResultLar5.mustPass
        invalidAUSResultLAR.mustFail
      }
      //Must pass if otherAUS is blank
      invalidAUSResultLAR
        .copy(
          ausResult = invalidAUSResultLAR.ausResult.copy(otherAusResult = ""))
        .mustPass
    }
  }
}
