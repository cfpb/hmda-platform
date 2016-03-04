package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.syntactical.ts.S028
import scalaz._
import Scalaz._
import scala.concurrent.ExecutionContext

trait TsPlatformValidationEngine extends TsValidationEngine {

  def validate(ts: TransmittalSheet)(implicit ec: ExecutionContext): ValidationNel[ValidationError, TransmittalSheet] = {

    def s028(t: TransmittalSheet): ValidationNel[ValidationError, TransmittalSheet] = {
      S028(t) match {
        case Success() => t.success
        case Failure(msg) => ValidationError(s"S028 failed: $msg").failure.toValidationNel
      }
    }

    (
      s010(ts)
      |@| s020(ts)
      //|@| s025(ts)
      |@| s100(ts)
      |@| s013(ts)
      |@| s028(ts)
    )((_, _, _, _, _) => ts)

  }

}

