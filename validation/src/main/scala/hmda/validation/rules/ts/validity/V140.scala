package hmda.validation.rules.ts.validity

import hmda.model.census.Census._
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V140 extends EditCheck[TransmittalSheet] {
  def apply(ts: TransmittalSheet): Result = {
    val resp = ts.respondent
    val stateCodes = states.keys.toList
    resp.state is containedIn(stateCodes)
  }

  override def name: String = "V140"
}
