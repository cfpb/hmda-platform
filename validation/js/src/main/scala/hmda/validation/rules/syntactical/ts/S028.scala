package hmda.validation.rules.syntactical.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Result, PlatformDsl, CommonDsl }

/*
 Timestamp must be numeric and in ccyymmddhhmm format
 */
object S028 extends CommonDsl with PlatformDsl {
  def apply(ts: TransmittalSheet): Result = {
    import scala.language.postfixOps
    val timestamp = ts.timestamp
    (timestamp is numeric) and (timestamp.toString is validTimestampFormat)
  }
}
