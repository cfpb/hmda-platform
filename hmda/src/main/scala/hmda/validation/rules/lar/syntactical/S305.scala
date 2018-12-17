package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S305 extends EditCheck[TransmittalLar] {
  override def name: String = "S305"
  override def apply(tsLar: TransmittalLar): ValidationResult = {

    println("Came inside S305 totalLines!: " + tsLar.ts.totalLines)
    println("Came inside S305 lars size!!!: " + tsLar.lars.size)

    tsLar.lars.size is equalTo(tsLar.lars.distinct.size)

  }

}
