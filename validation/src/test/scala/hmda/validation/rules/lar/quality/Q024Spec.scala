package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class Q024Spec extends LarEditCheckSpec {
  val config = ConfigFactory.load()
  val min_income = config.getInt("hmda.validation.quality.Q024.min_income_for_high_loan")

  // Cases meeting preconditions
  property("passes if actionTaken = 1, loan amount >= 5xincome, and income > 9") {
    forAll(larGen) { lar =>
      val income = lar.applicant.income
      whenever(income != "NA" && income.toInt > min_income) {
        val newLoan = lar.loan.copy(amount = income.toInt * 5 + 1)
        val newLar = lar.copy(actionTakenType = 1, loan = newLoan)
        newLar.mustPass
      }
    }
  }
  property("fails if actionTaken = 1, loan amount >= 5xincome, and income <= 9") {
    forAll(larGen, Gen.choose(1, min_income)) { (lar, i) =>
      val newLoan = lar.loan.copy(amount = i * 5 + 1)
      val newApplicant = lar.applicant.copy(income = i.toString)
      val newLar = lar.copy(actionTakenType = 1, loan = newLoan, applicant = newApplicant)
      newLar.mustFail
    }
  }

  // Cases not meeting preconditions
  property("passes if action taken type not 1") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 1) {
        lar.mustPass
      }
    }
  }

  property("passes if income is NA") {
    forAll(larGen) { lar =>
      val noIncomeApplicant = lar.applicant.copy(income = "NA")
      val newLar = lar.copy(applicant = noIncomeApplicant)
      newLar.mustPass
    }
  }
  property("passes if loan amount is < 5xincome") {
    forAll(larGen) { lar =>
      val income = lar.applicant.income
      whenever(income != "NA") {
        val newLoan = lar.loan.copy(amount = income.toInt * 5 - 1)
        val newLar = lar.copy(loan = newLoan)
        newLar.mustPass
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q024
}
