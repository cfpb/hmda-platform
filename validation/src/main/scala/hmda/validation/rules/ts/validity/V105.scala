package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._

/*
 Respondent name, address, city, state, and zip code must not = blank
 */
object V105 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    ts.respondent is completeNameAndAddress
  }

  override def name: String = "V105"

  override def description = ""
}
