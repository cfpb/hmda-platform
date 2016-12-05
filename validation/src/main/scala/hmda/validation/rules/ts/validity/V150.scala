package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V150 extends EditCheck[TransmittalSheet] {
  override def name: String = "V150"

  override def description = ""

  override def apply(ts: TransmittalSheet): Result = {
    ts.contact.name not equalTo(ts.respondent.name)
  }
}
