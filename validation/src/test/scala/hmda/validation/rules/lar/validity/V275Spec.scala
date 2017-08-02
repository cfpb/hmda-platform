package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V275Spec extends LarEditCheckSpec {
  property("All applications with no application date must pass") {
    forAll(larGen) { lar =>
      whenever(lar.loan.applicationDate == "NA") {
        lar.mustPass
      }
    }
  }

  val validDate: Gen[Int] = Gen.choose(20000101, 89991231)

  property("An application with the same application and action taken date must pass") {
    forAll(larGen, validDate) { (lar, date) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan, actionTakenDate = date)
      newLar.mustPass
    }
  }

  val dateDifference: Gen[Int] = Gen.choose(1, 10000000)

  property("An application with the action taken date after the application date must pass") {
    forAll(larGen, validDate, dateDifference) { (lar, date, difference) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan, actionTakenDate = date + difference)
      newLar.mustPass
    }
  }

  property("An application with the action taken date before the application date must fail") {
    forAll(larGen, validDate, dateDifference) { (lar, date, difference) =>
      val newLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(loan = newLoan, actionTakenDate = date - difference)
      newLar.mustFail
    }
  }

  val invalidApplicationDate: Gen[String] = Gen.alphaStr.filter(_ != "NA")

  property("An application with an invalid applicationDate must fail") {
    forAll(larGen, invalidApplicationDate) { (lar, notADate) =>
      val newLoan = lar.loan.copy(applicationDate = notADate)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V275
}
