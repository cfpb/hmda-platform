package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S304 extends EditCheck[TransmittalLar] {
  override def name: String = "S304"
  override def apply(tsLar: TransmittalLar): ValidationResult = {

    println("Came inside S304 totalLines!: " + tsLar.ts.totalLines)
    println("Came inside S304 lars size!!!: " + tsLar.lars.size)

    tsLar.ts.totalLines is equalTo(tsLar.lars.size)

  }

}
