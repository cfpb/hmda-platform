package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V385Spec extends LarEditCheckSpec {

  val actionTaken = List(3, 7)

  property("Succeeds if agency code is not 1 or actionTaken is not 3 or 7") {
    forAll(larGen) { lar =>
      whenever(lar.agencyCode != 1 || !actionTaken.contains(lar.actionTakenType)) {
        lar.mustPass
      }
    }
  }

  val actionTakenGen: Gen[Int] = Gen.oneOf(actionTaken)
  val validDenialGen: Gen[Int] = Gen.choose(1, 9)

  property("Succeeds if agency code = 1, actionTaken = 3,7, and denial reasons are valid") {
    forAll(larGen, actionTakenGen, validDenialGen) { (lar: LoanApplicationRegister, action: Int, denial: Int) =>
      val newDenial = lar.denial.copy(reason2 = denial.toString)
      val newLar = lar.copy(agencyCode = 1, actionTakenType = action, denial = newDenial)
      newLar.mustPass
    }
  }

  property("Fails if agency code = 1, actionTaken = 3,7 and denial reasons are incorrect") {
    forAll(larGen, actionTakenGen, validDenialGen) { (lar: LoanApplicationRegister, action: Int, denial: Int) =>
      val newDenial = lar.denial.copy(reason2 = "failure")
      val newLar = lar.copy(agencyCode = 1, actionTakenType = action, denial = newDenial)
      newLar.mustFail
    }
  }

  property("Fails if agency code = 1, actionTaken = 3,7, and no denial reason is provided") {
    forAll(larGen, actionTakenGen) { (lar: LoanApplicationRegister, action: Int) =>
      val newDenial = lar.denial.copy(reason1 = "", reason2 = "", reason3 = "")
      val newLar = lar.copy(agencyCode = 1, actionTakenType = action, denial = newDenial)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V385
}
