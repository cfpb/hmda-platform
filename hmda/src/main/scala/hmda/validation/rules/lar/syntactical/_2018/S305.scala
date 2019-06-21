package hmda.validation.rules.lar.syntactical._2018

import hmda.model.filing.ts._2018.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object S305 extends EditCheck[TransmittalLar] {
  override def name: String = "S305"
  override def apply(tsLar: TransmittalLar): ValidationResult = {

    tsLar.larsCount is equalTo(tsLar.larsDistinctCount.toInt)

  }

}
