package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V410Spec extends LarEditCheckSpec {

  import Gen.posNum

  property("succeeds when lien status is not 3") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 3) {
        lar.mustPass
      }
    }
  }

  property("succeeds when lien status is 3 and loan purpose is 2") {
    forAll(larGen) { lar =>
      val loan = lar.loan.copy(purpose = 2)
      val validLar = lar.copy(lienStatus = 3, loan = loan)
      validLar.mustPass
    }
  }

  property("fails when lien status is 3 and loan purpose is not 2") {
    forAll(larGen, posNum[Int]) { (lar: LoanApplicationRegister, x: Int) =>
      whenever(x != 2) {
        val loan = lar.loan.copy(purpose = x)
        val invalidLar = lar.copy(lienStatus = 3, loan = loan)
        invalidLar.mustFail
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V410
}
