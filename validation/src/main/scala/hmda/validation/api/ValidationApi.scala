package hmda.validation.api

import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.engine.ValidationError
import scalaz._
import scalaz.Scalaz._

trait ValidationApi {

  def convertResult[T](input: T, result: Result, ruleName: String): ValidationNel[ValidationError, T] = {
    result match {
      case Success() => input.success
      case Failure(msg) => ValidationError(s"$ruleName  failed: $msg").failure.toValidationNel
    }
  }

  def validateAll[E, T](checks: List[ValidationNel[E, T]], t: T): ValidationNel[E, T] = {
    checks.sequenceU.map {
      case c :: _ => c
      case Nil => t
    }
  }

}
