package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.TsValidationApi
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.syntactical.ts.{ S013, S100 }
import hmda.validation.rules.syntactical.{ S010, S020 }

import scala.concurrent.ExecutionContext
import scalaz.Scalaz._
import scalaz._

trait TsValidationEngine extends TsValidationApi {

  case class ValidationError(msg: String)

  type TsValidation = ValidationNel[ValidationError, TransmittalSheet]

  protected def s010(t: TransmittalSheet): TsValidation = {
    S010(t) match {
      case Success() => t.success
      case Failure(msg) => ValidationError(s"S010 failed: $msg").failure.toValidationNel
    }
  }

  protected def s020(t: TransmittalSheet): TsValidation = {
    S020(t) match {
      case Success() => t.success
      case Failure(msg) => ValidationError(s"S020 failed: $msg").failure.toValidationNel
    }
  }

  //TODO: Implement S025 validation rule
  //  protected def s025(t: TransmittalSheet): TsValidation = {
  //
  //  }

  protected def s100(t: TransmittalSheet): TsValidation = {
    S100(t, findYearProcessed) match {
      case Success() => t.success
      case Failure(msg) => ValidationError(s"S100 failed: $msg").failure.toValidationNel
    }
  }

  protected def s013(t: TransmittalSheet): TsValidation = {
    S013(t, findTimestamp) match {
      case Success() => t.success
      case Failure(msg) => ValidationError(s"S013 failed: $msg").failure.toValidationNel
    }
  }

  def validate(ts: TransmittalSheet): TsValidation = {

    (
      s010(ts)
      |@| s020(ts)
      //|@| s025(ts)
      |@| s100(ts)
      |@| s013(ts)
    )((_, _, _, _) => ts)
  }

}
