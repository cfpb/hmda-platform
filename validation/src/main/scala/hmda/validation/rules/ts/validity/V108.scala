package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V108 extends EditCheck[TransmittalSheet] {
  override def name: String = "V108"

  override def apply(ts: TransmittalSheet): Result = {
    when(ts.parent.name not equalTo("")) {
      ts.parent.name not equalTo(ts.respondent.name)
    }
  }
}
