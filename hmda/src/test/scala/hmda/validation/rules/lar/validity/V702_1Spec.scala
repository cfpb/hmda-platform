package scala.hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{EmptyAUSValue, OtherAUS}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity.V702_1

class V702_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V702_1
  property("Other AUS must have a value if any AUS field reported as other") {
    forAll(larGen) { lar =>
      val releventAUSLar1 = lar.copy(AUS = lar.AUS.copy(aus1 = OtherAUS))
      val releventAUSLar2 = lar.copy(AUS = lar.AUS.copy(aus2 = OtherAUS))
      val releventAUSLar3 = lar.copy(AUS = lar.AUS.copy(aus3 = OtherAUS))
      val releventAUSLar4 = lar.copy(AUS = lar.AUS.copy(aus4 = OtherAUS))
      val releventAUSLar5 = lar.copy(AUS = lar.AUS.copy(aus5 = OtherAUS))
      val irreleventAUS = lar.AUS.copy(
        aus1 = EmptyAUSValue,
        aus2 = EmptyAUSValue,
        aus3 = EmptyAUSValue,
        aus4 = EmptyAUSValue,
        aus5 = EmptyAUSValue
      )

      lar.copy(AUS = irreleventAUS).mustPass

      whenever(lar.AUS.otherAUS != "") {
        releventAUSLar1.mustPass
        releventAUSLar2.mustPass
        releventAUSLar3.mustPass
        releventAUSLar4.mustPass
        releventAUSLar5.mustPass
      }

      releventAUSLar1
        .copy(AUS = releventAUSLar1.AUS.copy(otherAUS = ""))
        .mustFail
      releventAUSLar2
        .copy(AUS = releventAUSLar2.AUS.copy(otherAUS = ""))
        .mustFail
      releventAUSLar3
        .copy(AUS = releventAUSLar3.AUS.copy(otherAUS = ""))
        .mustFail
      releventAUSLar4
        .copy(AUS = releventAUSLar4.AUS.copy(otherAUS = ""))
        .mustFail
      releventAUSLar5
        .copy(AUS = releventAUSLar5.AUS.copy(otherAUS = ""))
        .mustFail
    }
  }
}
