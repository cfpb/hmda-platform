package hmda.validation.rules.lar.syntactical._2019

import hmda.model.filing.ts._2019.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object S304 extends EditCheck[TransmittalLar] {
  override def name: String = "S304"
  override def apply(tsLar: TransmittalLar): ValidationResult = {
    tsLar.ts.totalLines is equalTo(tsLar.larsCount)
  }
}
