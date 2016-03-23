package hmda.validation.engine.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.api.ts.TsValidationApi
import hmda.validation.engine.ValidationError
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.syntactical._

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Scalaz._
import scalaz._

trait TsSyntacticalEngine extends TsCommonEngine with ValidationApi with TsValidationApi {

  private def s010(t: TransmittalSheet): TsValidation = {
    convertResult(t, S010(t), "S010")
  }

  private def s020(t: TransmittalSheet): TsValidation = {
    convertResult(t, S020(t), "S020")
  }

  //TODO: Implement S025 validation rule
  //  private def s025(t: TransmittalSheet): TsValidation = {
  //
  //  }

  private def s100(t: TransmittalSheet): Future[TsValidation] = {
    S100(t, findYearProcessed).map { result =>
      convertResult(t, result, "S100")
    }
  }

  private def s013(t: TransmittalSheet): Future[TsValidation] = {
    S013(t, findTimestamp).map { result =>
      convertResult(t, result, "S013")
    }
  }

  private def s028(t: TransmittalSheet): TsValidation = {
    convertResult(t, S028(t), "S028")
  }

  def validate(ts: TransmittalSheet): Future[TsValidation] = {

    val checks = List(
      s010(ts),
      s020(ts),
      s028(ts)
    )

    val fs100 = s100(ts)
    val fs013 = s013(ts)

    for {
      f100 <- fs100
      f013 <- fs013
    } yield {
      (
        validateAllT(checks, ts)
        |@| f100
        |@| f013
      )((_, _, _) => ts)
    }
  }

}
