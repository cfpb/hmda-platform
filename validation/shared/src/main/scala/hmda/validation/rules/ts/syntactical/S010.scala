package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Result, CommonDsl }

/*
 The first record in the file mest = 1 (TS)
 */

object S010 extends CommonDsl {

  //Hardcoded for now
  val tsId = 1

  def apply(ts: TransmittalSheet): Result = {
    ts.id is equalTo(tsId)
  }
}
