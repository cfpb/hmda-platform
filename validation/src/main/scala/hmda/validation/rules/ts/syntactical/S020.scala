package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ CommonDsl, Result }

object S020 extends CommonDsl {
  //Hardcoded for now
  val agencyCodes: List[Int] = List(1, 2, 3, 5, 7, 9)

  def apply(ts: TransmittalSheet): Result = {
    ts.agencyCode is containedIn(agencyCodes)
  }
}
