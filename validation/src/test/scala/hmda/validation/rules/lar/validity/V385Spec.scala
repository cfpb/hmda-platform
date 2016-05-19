package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V385Spec extends LarEditCheckSpec {

  val actionTaken = List(3, 7)

  property("If agency code is not 1 or actionTaken is not 3 or 7, lar must succeed") {
    forAll(larGen) { lar =>
      whenever(lar.agencyCode != 1 || !actionTaken.contains(lar.actionTakenType)) {
        lar.mustPass
      }
    }
  }

  val actionTakenGen: Gen[Int] = Gen.oneOf(actionTaken)
  val validDenialGen: Gen[Int] = Gen.choose(1, 9)

  property("If the agency code and actionTaken are correct and denial reasons are correct, lar must pass") {
    forAll(larGen, actionTakenGen, validDenialGen) { (lar: LoanApplicationRegister, action: Int, denial: Int) =>
      val newDenial = lar.denial.copy(reason2 = denial.toString)
      val newLar = lar.copy(agencyCode = 1, actionTakenType = action, denial = newDenial)
      newLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V385
}
