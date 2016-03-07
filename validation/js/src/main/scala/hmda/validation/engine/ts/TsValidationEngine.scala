package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.ts.syntactical.S028
import scalaz._
import Scalaz._
import scala.concurrent.{ Future, ExecutionContext }

trait TsValidationEngine extends CommonTsValidationEngine {

  override def validate(ts: TransmittalSheet)(implicit ec: ExecutionContext): Future[TsValidation] = {

    def s028(t: TransmittalSheet): ValidationNel[ValidationError, TransmittalSheet] = {
      S028(t) match {
        case Success() => t.success
        case Failure(msg) => ValidationError(s"S028 failed: $msg").failure.toValidationNel
      }
    }

    val fs100 = s100(ts)
    val fs013 = s013(ts)

    for {
      f100 <- fs100
      f013 <- fs013
    } yield {
      (
        s010(ts)
        |@| s020(ts)
        |@| f100
        |@| f013
        |@| s028(ts)
      )((_, _, _, _, _) => ts)
    }

  }

}

