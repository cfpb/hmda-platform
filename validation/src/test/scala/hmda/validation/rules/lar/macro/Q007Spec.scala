package hmda.validation.rules.lar.`macro`

import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

class Q007Spec extends MacroSpec {

  property("Valid if total number of approved but not accepted is less than or equal to 0.15 * total") {
    larSource.mustPass
  }

  property("Invalid if total number of approved but not accepted is greater than 0.15 * total") {
    val badLar = lars.head.copy(actionTakenType = 2)
    val newLars = lars ++ Array(badLar)
    val newLarSource = Source.fromIterator(() => newLars.toIterator)
    newLarSource.mustFail

  }

  override def check: AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = Q007
}
