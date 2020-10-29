package hmda.validation.rules.lar.quality._2020


import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

object Q600_warning extends EditCheck[TransmittalLar] {
  override def name: String = "Q600-warning"
  override def apply(tsLar: TransmittalLar): ValidationResult =
    tsLar.ts.totalLines is equalTo(tsLar.uniqueLarsSpecificFields.toInt)
}
