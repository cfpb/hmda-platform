package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V210Spec extends LarEditCheckSpec {
  property("All applications must pass") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val invalidDate: Gen[String] = Gen.alphaStr.filter(_ != "NA")

  property("An application with an invalid date must fail") {
    forAll(larGen, invalidDate) { (lar: LoanApplicationRegister, date: String) =>
      val badLoan = lar.loan.copy(applicationDate = date)
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  property("An application dated with an incorrect century must fail") {
    forAll(larGen) { lar =>
      val badLoan = lar.loan.copy(applicationDate = "19990101")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  property("An application missing part of the date must fail") {
    forAll(larGen) { lar =>
      val badLoan = lar.loan.copy(applicationDate = "200001")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  property("An application with an invalid month must fail") {
    forAll(larGen) { lar =>
      val badLoan = lar.loan.copy(applicationDate = "20001301")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  property("An application with an invalid day must fail") {
    forAll(larGen) { lar =>
      val badLoan = lar.loan.copy(applicationDate = "20001232")
      val badLar = lar.copy(loan = badLoan)
      badLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V210
}
