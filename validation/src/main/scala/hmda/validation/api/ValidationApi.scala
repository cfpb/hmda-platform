package hmda.validation.api

import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.engine._
import hmda.validation.rules.EditCheck

import scalaz._
import scalaz.Scalaz._

trait ValidationApi {

  def check[T](editCheck: EditCheck[T], input: T, inputId: String, errorType: ValidationErrorType, ts: Boolean): ValidationNel[ValidationError, T] = {
    convertResult(input, editCheck(input), editCheck.name, inputId, errorType, ts)
  }

  def convertResult[T](input: T, result: Result, ruleName: String, inputId: String, errorType: ValidationErrorType, ts: Boolean): ValidationNel[ValidationError, T] = {
    result match {
      case Success() => input.success
      case Failure() => errorType match {
        case Syntactical => SyntacticalValidationError(inputId, ruleName, ts).failure.toValidationNel
        case Validity => ValidityValidationError(inputId, ruleName, ts).failure.toValidationNel
        case Quality => QualityValidationError(inputId, ruleName, ts).failure.toValidationNel
        case Macro => MacroValidationError(ruleName).failure.toValidationNel
      }
    }
  }

  def validateAll[E, T](checks: List[ValidationNel[E, T]], input: T): ValidationNel[E, T] = {
    checks.sequenceU.map {
      case c :: _ => c
      case Nil => input
    }
  }

}
