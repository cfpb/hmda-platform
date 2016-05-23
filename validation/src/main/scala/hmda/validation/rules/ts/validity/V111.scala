package hmda.validation.rules.ts.validity

import hmda.model.census.Census._
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

/*
 Respondent name, address, city, state, and zip code must not = blank
 */
object V111 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    ts.parent.state is containedIn(states.keys.toList)
  }

  override def name: String = "V111"
}
