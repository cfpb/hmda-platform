package hmda.validation.rules.syntactical.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ CommonDsl, Result }

object S013 extends CommonDsl {
  def apply(ts: TransmittalSheet, timestamp: Long): Result = {
    val t = ts.timestamp
    t is greaterThan(timestamp)
  }
}
