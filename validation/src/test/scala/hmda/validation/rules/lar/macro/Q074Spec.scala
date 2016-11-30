package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q074Spec extends MacroSpec {

  val numOfLoanApplications = config.getInt("hmda.validation.macro.Q074.numOfLoanApplications")
  val denialMultiplier = config.getDouble("hmda.validation.macro.Q074.numOfLarsMultiplier")

  def irrelevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 2, loanType = 2, purpose = 3)
    lar.copy(actionTakenType = 1, loan = relevantLoan)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 2, loanType = 2, purpose = 3)
    lar.copy(actionTakenType = 6, loan = relevantLoan)
  }

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(1, numOfLoanApplications - 1)
  val relevantAmount: Gen[Int] = Gen.chooseNum(numOfLoanApplications, 10000)

  property(s"be valid if fewer than $numOfLoanApplications specified loans") {
    forAll(irrelevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, x, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $numOfLoanApplications specified loans and count > $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt + 1
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be invalid if more than $numOfLoanApplications specified loans and count = $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt
      val lars = larNGen(x).sample.getOrElse(Nil)
      val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  property(s"be invalid if more than $numOfLoanApplications specified loans and count < $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt - 1
      val lars = larNGen(x).sample.getOrElse(Nil)
      val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q074
}
