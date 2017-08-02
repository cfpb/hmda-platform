package hmda.validation.rules.lar.`macro`
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen

class Q065Spec extends MacroSpec {

  val threshold = config.getInt("hmda.validation.macro.Q065.hoepaLoanLimit")

  def relevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(hoepaStatus = 1)
  }
  def irrelevantLar(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(hoepaStatus = 2)
  }

  def nonHoepaLoans: Gen[Int] = Gen.choose(0, 1000)

  property(s"be valid if there are fewer than $threshold HOEPA loans") {
    forAll(nonHoepaLoans) { extras =>
      val belowThreshold = threshold - 1
      val lars = larNGen(belowThreshold + extras).sample.getOrElse(List[LoanApplicationRegister]())
      val larSource = newLarSource(lars, belowThreshold, relevantLar, irrelevantLar)
      larSource.mustPass
    }
  }

  property(s"be invalid if there are $threshold HOEPA loans") {
    forAll(nonHoepaLoans) { extras =>
      val lars = larNGen(threshold + extras).sample.getOrElse(List[LoanApplicationRegister]())
      val larSource = newLarSource(lars, threshold, relevantLar, irrelevantLar)
      larSource.mustFail
    }
  }

  property(s"be invalid if there are more than $threshold HOEPA loans") {
    forAll(nonHoepaLoans) { extras =>
      val overThreshold = threshold + 1
      val lars = larNGen(overThreshold + extras).sample.getOrElse(List[LoanApplicationRegister]())
      val larSource = newLarSource(lars, overThreshold, relevantLar, irrelevantLar)
      larSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q065
}
