package hmda.validation.rules.lar.syntactical

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

object S307 extends EditCheck[TransmittalLar] {
  override def name: String = "S307"

  override def apply(tsLar: TransmittalLar): ValidationResult =
    (tsLar.actionTakenDatesWithinRange is greaterThan(0L)) and
      (tsLar.actionTakenDatesGreaterThanRange is equalTo(0L))
}