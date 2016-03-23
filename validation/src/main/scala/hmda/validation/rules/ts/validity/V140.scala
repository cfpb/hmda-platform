package hmda.validation.rules.ts.validity

import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.model.census.Census._
import hmda.model.fi.ts.{ Respondent, TransmittalSheet }

object V140 extends CommonDsl {
  def apply(ts: TransmittalSheet): Result = {
    val resp = ts.respondent
    val stateCodes = states.keys.toList
    resp.state is containedIn(stateCodes)
  }
}
