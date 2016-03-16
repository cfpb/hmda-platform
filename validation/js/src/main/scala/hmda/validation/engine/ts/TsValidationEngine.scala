package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.engine.ValidationError
import hmda.validation.rules.ts.syntactical.S028
import scalaz._
import Scalaz._
import scala.concurrent.{ Future, ExecutionContext }

trait TsValidationEngine extends CommonTsValidation {

  override def validate(ts: TransmittalSheet): Future[TsValidation] = {

    def s028(t: TransmittalSheet): ValidationNel[ValidationError, TransmittalSheet] = {
      convertResult(t, S028(t), "S028")
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

