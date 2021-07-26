package hmda.validation.rules.lar.syntactical

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

/**
 * S306 is responsible for detecting rows with duplicate ULI and where actionTakenType == 1
 */
object S306 extends EditCheck[TransmittalLar] {
  override def name: String = "S306"

  override def apply(tsLar: TransmittalLar): ValidationResult =
    tsLar.duplicateUliToLineNumbersUliActionType.size is equalTo(0)

}