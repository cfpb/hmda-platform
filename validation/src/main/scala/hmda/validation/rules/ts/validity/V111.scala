package hmda.validation.rules.ts.validity

import hmda.model.census.Census._
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

/*
 Parent state must be a proper state abbreviation
 */
object V111 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    when(ts.parent.state not empty) {
      ts.parent.state is containedIn(states.keySet)
    }
  }

  override def name: String = "V111"

  override def description = ""
}
