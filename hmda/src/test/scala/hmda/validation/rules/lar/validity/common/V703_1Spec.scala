package scala.hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  EmptyAUSResultValue,
  OtherAutomatedUnderwritingResult
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity.V703_1

class V703_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V703_1
  property("Other AUS must have a value if any AUS field reported as other") {
    forAll(larGen) { lar =>
      val releventAUSResultLar1 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult1 = OtherAutomatedUnderwritingResult))
      val releventAUSResultLar2 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult2 = OtherAutomatedUnderwritingResult))
      val releventAUSResultLar3 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult3 = OtherAutomatedUnderwritingResult))
      val releventAUSResultLar4 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult4 = OtherAutomatedUnderwritingResult))
      val releventAUSResultLar5 = lar.copy(
        ausResult =
          lar.ausResult.copy(ausResult5 = OtherAutomatedUnderwritingResult))
      val irreleventAUSResult = lar.ausResult.copy(
        ausResult1 = EmptyAUSResultValue,
        ausResult2 = EmptyAUSResultValue,
        ausResult3 = EmptyAUSResultValue,
        ausResult4 = EmptyAUSResultValue,
        ausResult5 = EmptyAUSResultValue
      )
      lar.copy(ausResult = irreleventAUSResult).mustPass
      whenever(lar.ausResult.otherAusResult != "") {
        releventAUSResultLar1.mustPass
        releventAUSResultLar2.mustPass
        releventAUSResultLar3.mustPass
        releventAUSResultLar4.mustPass
        releventAUSResultLar5.mustPass
      }
      releventAUSResultLar1
        .copy(
          ausResult = releventAUSResultLar1.ausResult.copy(otherAusResult = ""))
        .mustFail
      releventAUSResultLar2
        .copy(
          ausResult = releventAUSResultLar2.ausResult.copy(otherAusResult = ""))
        .mustFail
      releventAUSResultLar3
        .copy(
          ausResult = releventAUSResultLar3.ausResult.copy(otherAusResult = ""))
        .mustFail
      releventAUSResultLar4
        .copy(
          ausResult = releventAUSResultLar4.ausResult.copy(otherAusResult = ""))
        .mustFail
      releventAUSResultLar5
        .copy(
          ausResult = releventAUSResultLar5.ausResult.copy(otherAusResult = ""))
        .mustFail
    }
  }
}
