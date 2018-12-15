package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S304 extends EditCheck[TransmittalSheet] {
  override def name: String = "S304"
//
  override def apply(ts: TransmittalSheet): ValidationResult = {

    println("Came inside S304!!!!: " + ts)

    ts.id is equalTo(1)
  }

}
