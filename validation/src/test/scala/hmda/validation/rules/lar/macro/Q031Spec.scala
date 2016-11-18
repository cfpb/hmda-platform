package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q031Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val larCount = config.getInt("hmda.validation.macro.Q031.numOfLars")
  val multifamilyCount = config.getInt("hmda.validation.macro.Q031.numOfMultifamily")

  def irrelevantLar(lar: LoanApplicationRegister) = {
    val irrelevantLoan = lar.loan.copy(propertyType = 2)
    lar.copy(loan = irrelevantLoan)
  }
  def relevantLar(lar: LoanApplicationRegister) = {
    val relevantLoan = lar.loan.copy(propertyType = 3)
    lar.copy(loan = relevantLoan)
  }

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(larCount, 10000)
  val relevantAmount: Gen[Int] = Gen.chooseNum(200, larCount - 1)

  property(s"be valid if more than $larCount lars") {
    forAll(irrelevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, x, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if less than $larCount lars and less than $multifamilyCount multifamily loans") {
    forAll(relevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val validLarSource = newLarSource(lars, 1, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be invalid if less than $larCount lars and more than $multifamilyCount multifamily loans") {
    forAll(relevantAmount) { (x) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
      val invalidLarSource = newLarSource(lars, multifamilyCount, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q031
}
