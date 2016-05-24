package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V215Spec extends LarEditCheckSpec {
  property("Passes if application date is NA") {
    forAll(larGen) { lar =>
      val naLoan = lar.loan.copy(applicationDate = "NA")
      val newLar = lar.copy(loan = naLoan)
      newLar.mustPass
    }
  }

  property("Passes if actionTakenType is not 6") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6) {
        lar.mustPass
      
    }
  }

  val invalidDate: Gen[String] = Gen.alphaStr.filter(_ != "NA")

  property("Fails if actionTakenType is 6 and date application received is not NA") {
    forAll(larGen, invalidDate) { (lar, altDate) =>
      val naLoan = lar.loan.copy(applicationDate = altDate)
      val newLar = lar.copy(loan = naLoan, actionTakenType = 6)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V215
}
