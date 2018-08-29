package hmda.validation.api

import cats.data._
import hmda.model.validation._
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.EditCheck
import cats.implicits._

trait ValidationApi {

  type HmdaValidation[A] = ValidatedNel[ValidationError, A]

  def check[A](editCheck: EditCheck[A],
               input: A,
               errorId: String,
               validationErrorType: ValidationErrorType): HmdaValidation[A] = {
    convertResult(input,
                  editCheck(input),
                  editCheck.parent,
                  errorId,
                  validationErrorType)
  }

  def convertResult[A](
      input: A,
      result: ValidationResult,
      editName: String,
      uli: String,
      validationErrorType: ValidationErrorType): HmdaValidation[A] =
    result match {

      case ValidationSuccess => input.validNel

      case ValidationFailure =>
        validationErrorType match {
          case Syntactical =>
            SyntacticalValidationError(uli, editName).invalidNel
          case Validity => ValidityValidationError(uli, editName).invalidNel
          case Quality  => QualityValidationError(uli, editName).invalidNel
          case Macro    => MacroValidationError(editName).invalidNel
        }
    }

}
