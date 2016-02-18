package hmda.validation.rules.syntactical.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.HmdaDSL._
import hmda.validation.dsl.Result

/*
 Activity year must = year being processed (i.e. = 2016)
 */
object S100 {

  def apply(ts: TransmittalSheet, year: Int): Result = {
    ts.activityYear is equalTo(year)
  }

}
