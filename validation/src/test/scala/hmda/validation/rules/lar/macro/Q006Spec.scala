package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q006Spec extends MacroSpec {

  val numOfOriginatedHomePurchaseLoans = config.getInt("hmda.validation.macro.Q006.numOfOriginatedHomePurchaseLoans")
  val multiplier = config.getDouble("hmda.validation.macro.Q006.numOfLarsMultiplier")

  def irrelevantLar(lar: LoanApplicationRegister) = {
    val irrelevantLoan = lar.loan.copy(purpose = 1)
    lar.copy(actionTakenType = 2, loan = irrelevantLoan)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(purpose = 1)
    lar.copy(actionTakenType = 1, loan = relevantLoan)
  }

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(1, numOfOriginatedHomePurchaseLoans)
  val relevantAmount: Gen[Int] = Gen.chooseNum(numOfOriginatedHomePurchaseLoans + 1, 10000)

  property(s"be valid if fewer than $numOfOriginatedHomePurchaseLoans originated loans") {
    forAll(irrelevantAmount) { (totalLar) =>
      val lars = larNGen(totalLar).sample.getOrElse(List[LoanApplicationRegister]())
      val validLarSource = newLarSource(lars, totalLar, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $numOfOriginatedHomePurchaseLoans originated loans and originated < $multiplier * total") {
    forAll(relevantAmount) { (totalLar) =>
      val numOfLars = (totalLar / multiplier).toInt + 1
      val lars = larNGen(numOfLars).sample.getOrElse(List[LoanApplicationRegister]())
      val validLarSource = newLarSource(lars, totalLar, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $numOfOriginatedHomePurchaseLoans originated loans and originated = $multiplier * total") {
    forAll(relevantAmount) { (totalLar) =>
      val numOfLars = math.ceil(totalLar / multiplier).toInt
      val lars = larNGen(numOfLars).sample.getOrElse(List[LoanApplicationRegister]())
      val validLarSource = newLarSource(lars, totalLar, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be invalid if more than $numOfOriginatedHomePurchaseLoans originated loans and originated > $multiplier * total") {
    forAll(relevantAmount) { (totalLar) =>
      val numOfLars = (totalLar / multiplier).toInt - 1
      val lars = larNGen(numOfLars).sample.getOrElse(List[LoanApplicationRegister]())
      val invalidLarSource = newLarSource(lars, totalLar, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q006
}
