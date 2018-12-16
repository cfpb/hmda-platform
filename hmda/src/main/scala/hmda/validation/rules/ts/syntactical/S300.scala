package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S300 extends EditCheck[TransmittalLar] {
  override def name: String = "S300"

  override def apply(ts: TransmittalLar): ValidationResult = {

    println("Came in S300!!!!")
    ts.ts.id is equalTo(1)
  }
}
