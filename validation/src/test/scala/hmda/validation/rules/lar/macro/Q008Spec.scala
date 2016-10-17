package hmda.validation.rules.lar.`macro`
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q008Spec extends MacroSpec {

  "Q008" must {
    "be valid if withdrawn <= 0.30 * total" in {
      larSource.mustPass
    }

    "be invalid if withdrawn > 0.30 * total" in {
      val badLar1 = lars.head.copy(actionTakenType = 4)
      val badLar2 = lars.head.copy(actionTakenType = 4)
      val newLars = lars ++ Array(badLar1, badLar2)
      val newLarSource = Source.fromIterator(() => newLars.toIterator)
      newLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q008
}
