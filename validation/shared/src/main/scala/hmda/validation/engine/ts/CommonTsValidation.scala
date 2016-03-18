package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.TsValidationApi
import hmda.validation.dsl.{ Result, Failure, Success }
import hmda.validation.engine.ValidationError
import hmda.validation.rules.ts.syntactical.{ S020, S010, S100, S013 }
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Scalaz._
import scalaz._

trait CommonTsValidation extends TsValidationApi {

  type TsValidation = ValidationNel[ValidationError, TransmittalSheet]

  implicit val ec: ExecutionContext

  protected def s010(t: TransmittalSheet): TsValidation = {
    convertResult(t, S010(t), "S010")
  }

  protected def s020(t: TransmittalSheet): TsValidation = {
    convertResult(t, S020(t), "S020")
  }

  //TODO: Implement S025 validation rule
  //  protected def s025(t: TransmittalSheet): TsValidation = {
  //
  //  }

  protected def s100(t: TransmittalSheet): Future[TsValidation] = {
    S100(t, findYearProcessed).map { result =>
      convertResult(t, result, "S100")
    }
  }

  protected def s013(t: TransmittalSheet): Future[TsValidation] = {
    S013(t, findTimestamp).map { result =>
      convertResult(t, result, "S013")
    }
  }

  protected def convertResult[A](input: A, result: Result, ruleName: String): ValidationNel[ValidationError, A] = {
    result match {
      case Success() => input.success
      case Failure(msg) => ValidationError(ruleName + s" failed: $msg").failure.toValidationNel
    }
  }

  def validate(ts: TransmittalSheet): Future[TsValidation] = {

    val fs100 = s100(ts)
    val fs013 = s013(ts)

    for {
      a <- fs100
      b <- fs013
    } yield {
      (
        s010(ts)
        |@| s020(ts)
        |@| a
        |@| b
      )((_, _, _, _) => ts)
    }

  }

}
