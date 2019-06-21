package hmda.validation.rules.ts.syntactical._2018

import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object S300 extends EditCheck[TransmittalSheet] {
  override def name: String = "S300"

  override def apply(ts: TransmittalSheet): ValidationResult = {
    ts.id is equalTo(1)
  }
}
