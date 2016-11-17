package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q056Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val conventionalCount = config.getInt("hmda.validation.macro.Q056.numOfConventionalHomePurchaseLoans")
  val denialMultiplier = config.getDouble("hmda.validation.macro.Q056.deniedConventionalHomePurchaseLoansMultiplier")

  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(purpose = 1).copy(loanType = 1)
    lar.copy(actionTakenType = 3).copy(loan = relevantLoan)
  }

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(1, conventionalCount - 1)
  val relevantAmount: Gen[Int] = Gen.chooseNum(conventionalCount, 10000)

  def makeLarsRelevant(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(purpose = 1).copy(loanType = 1)
    lar.copy(loan = relevantLoan)
  }

  property(s"be valid if fewer than $conventionalCount conventional loans") {
    forAll(irrelevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => makeLarsRelevant(lar))
      val validLarSource = newLarSource(lars, x, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $conventionalCount conventional loans and denials < $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt - 1
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => makeLarsRelevant(lar))
      val validLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $conventionalCount conventional loans and denials = $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => makeLarsRelevant(lar))
      val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      invalidLarSource.mustPass
    }
  }

  property(s"be invalid if more than $conventionalCount conventional loans and denials > $denialMultiplier * total") {
    forAll(relevantAmount) { (x) =>
      val numOfRelevantLars = (x * denialMultiplier).toInt + 1
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => makeLarsRelevant(lar))
      val invalidLarSource = newLarSource(lars, numOfRelevantLars, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q056
}
