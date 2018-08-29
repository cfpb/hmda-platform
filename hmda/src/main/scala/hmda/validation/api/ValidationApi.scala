package hmda.validation.api

import cats.Semigroup
import cats.data._
import hmda.model.validation._
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.EditCheck
import cats.implicits._

trait ValidationApi[A] {

  implicit val sg = new Semigroup[A] {
    override def combine(x: A, y: A): A = x
  }

  type HmdaValidation[B] = ValidatedNel[ValidationError, B]

  def check[B](editCheck: EditCheck[B],
               input: B,
               errorId: String,
               validationErrorType: ValidationErrorType): HmdaValidation[B] = {
    convertResult(input,
                  editCheck(input),
                  editCheck.parent,
                  errorId,
                  validationErrorType)
  }

  def convertResult[B](
      input: B,
      result: ValidationResult,
      editName: String,
      uli: String,
      validationErrorType: ValidationErrorType): HmdaValidation[B] =
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
