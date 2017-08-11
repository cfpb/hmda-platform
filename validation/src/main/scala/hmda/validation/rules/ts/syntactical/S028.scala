package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateHmda._

/*
 Timestamp must be numeric and in ccyymmddhhmm format
 */
object S028 extends EditCheck[TransmittalSheet] {

  // The parser ensures timestamp is numeric, so the first clause
  //  of this edit check will never fail. (If timestamp is not numeric,
  //  file will not parse.)
  override def apply(ts: TransmittalSheet): Result = {
    val timestamp = ts.timestamp
    (timestamp is numeric) and (timestamp.toString is validTimestampFormat)
  }

  override def name: String = "S028"
}
