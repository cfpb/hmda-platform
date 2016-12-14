package hmda.validation.rules.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q020 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    ts.respondent.address not equalTo(ts.parent.address)
  }

  override def name: String = "Q020"

}
