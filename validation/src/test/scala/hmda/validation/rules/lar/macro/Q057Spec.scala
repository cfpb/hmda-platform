package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q057Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val larCount = config.getInt("hmda.validation.macro.Q057.numOfLoanApplications")

  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  def relevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 3)

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(1, larCount - 1)
  val relevantAmount: Gen[Int] = Gen.chooseNum(larCount, 10000)

  property(s"be valid if fewer than $larCount lars") {
    forAll(irrelevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, x, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $larCount lars and one denial") {
    forAll(relevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, 1, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be invalid if more than $larCount lars and no denials") {
    forAll(relevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val invalidLarSource = newLarSource(lars, 0, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q057
}
