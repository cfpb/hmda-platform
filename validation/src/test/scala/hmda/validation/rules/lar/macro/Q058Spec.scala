package hmda.validation.rules.lar.`macro`
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q058Spec extends MacroSpec {

  val config = ConfigFactory.load()
  val preapprovalCount = config.getInt("hmda.validation.macro.Q058.numOfPreapprovalsRequested")

  def irrelevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 2)
  def relevantLar(lar: LoanApplicationRegister) = lar.copy(actionTakenType = 7)

  val irrelevantAmount: Gen[Int] = Gen.chooseNum(1, preapprovalCount - 1)
  val relevantAmount: Gen[Int] = Gen.chooseNum(preapprovalCount, 10000)

  property(s"be valid if fewer than $preapprovalCount preapproval requests") {
    forAll(irrelevantAmount) { (x: Int) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => lar.copy(preapprovals = 1))
      val validLarSource = newLarSource(lars, x, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be valid if more than $preapprovalCount preapproval requests and one denial") {
    forAll(relevantAmount) { (x: Int) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => lar.copy(preapprovals = 1))
      val validLarSource = newLarSource(lars, 1, relevantLar, irrelevantLar)
      validLarSource.mustPass
    }
  }

  property(s"be invalid if more than $preapprovalCount preapproval requests and no denials") {
    forAll(relevantAmount) { (x: Int) =>
      val lars = larNGen(x).sample.getOrElse(Nil)
        .map(lar => lar.copy(preapprovals = 1))
      val invalidLarSource = newLarSource(lars, 0, relevantLar, irrelevantLar)
      invalidLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q058
}
