package hmda.validation.rules.lar.`macro`

import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q007Spec extends MacroSpec {

  "Q007" must {
    "be valid if not accepted <= 0.15 * total" in {
      larSource.mustPass
    }

    "be invalid if not accepted > 0.15 * total" in {
      val badLar = lars.head.copy(actionTakenType = 2)
      val newLars = lars ++ Array(badLar)
      val newLarSource = Source.fromIterator(() => newLars.toIterator)
      newLarSource.mustFail
    }
  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q007
}
