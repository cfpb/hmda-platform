package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q038Spec extends LarEditCheckSpec with BadValueUtils {
  val config = ConfigFactory.load()
  val loanAmount = config.getInt("hmda.validation.quality.Q038.loan.amount")

  property(s"All lars with loan amounts <= $loanAmount must pass") {
    forAll(larGen, Gen.choose(0, loanAmount)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val irrelevantLien: Gen[Int] = intOtherThan(3)

  property("Whenever lien status is not 3, lar must pass") {
    forAll(larGen, irrelevantLien) { (lar, x) =>
      val newLar = lar.copy(lienStatus = x)
      newLar.mustPass
    }
  }

  property(s"A lar with lien status equal to 3 and income > $loanAmount must fail") {
    forAll(larGen, Gen.choose(loanAmount + 1, Int.MaxValue)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(lienStatus = 3, loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q038
}
