package hmda.validation.rules.fi.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.HmdaDSL._

/*
 Activity year must = year being processed (i.e. = 2016)
 */
object S100 {

  def apply(ts: TransmittalSheet, year: Int) = {
    ts.activityYear shouldBe equalTo(year)
  }

}
