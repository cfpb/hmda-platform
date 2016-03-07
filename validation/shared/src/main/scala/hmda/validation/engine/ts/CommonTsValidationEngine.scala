package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.TsValidationApi
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.ts.syntactical.{ S020, S010, S100, S013 }
import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Scalaz._
import scalaz._

trait CommonTsValidationEngine extends TsValidationApi {

  case class ValidationError(msg: String)

  type TsValidation = ValidationNel[ValidationError, TransmittalSheet]

  implicit val ec: ExecutionContext

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

  protected def s100(t: TransmittalSheet)(implicit ec: ExecutionContext): Future[TsValidation] = {
    S100(t, findYearProcessed).map { result =>
      result match {
        case Success() => t.success
        case Failure(msg) => ValidationError(s"s100 failed: $msg").failure.toValidationNel
      }
    }
  }

  protected def s013(t: TransmittalSheet): Future[TsValidation] = {
    S013(t, findTimestamp).map { result =>
      result match {
        case Success() => t.success
        case Failure(msg) => ValidationError(s"S013 failed: $msg").failure.toValidationNel
      }
    }
  }

  def validate(ts: TransmittalSheet)(implicit ec: ExecutionContext): Future[TsValidation] = {

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
