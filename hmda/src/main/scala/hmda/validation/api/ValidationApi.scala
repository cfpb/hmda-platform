package hmda.validation.api

import cats.implicits._
import hmda.model.validation._
import hmda.validation._
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.{ AsyncEditCheck, EditCheck }

import scala.concurrent.{ ExecutionContext, Future }

trait ValidationApi[A] {

  def check[B](
                editCheck: EditCheck[B],
                input: B,
                errorId: String,
                validationErrorType: ValidationErrorType,
                validationErrorEntity: ValidationErrorEntity
              ): HmdaValidation[B] =
    convertResult(input, editCheck(input), editCheck.name, errorId, validationErrorType, validationErrorEntity)

  def checkAsync[B](
                     asyncEditCheck: AsyncEditCheck[B],
                     input: B,
                     errorId: String,
                     validationErrorType: ValidationErrorType,
                     validationErrorEntity: ValidationErrorEntity
                   )(implicit ec: ExecutionContext): Future[HmdaValidation[B]] =
    convertResultAsync(
      input,
      asyncEditCheck(input),
      asyncEditCheck.name,
      errorId,
      validationErrorType,
      validationErrorEntity
    )

  def convertResult[B](
                        input: B,
                        result: ValidationResult,
                        editName: String,
                        uli: String,
                        validationErrorType: ValidationErrorType,
                        validationErrorEntity: ValidationErrorEntity
                      ): HmdaValidation[B] =
    resultToValidationError(input, result, editName, uli, validationErrorType, validationErrorEntity)

  def convertResultAsync[B](
                             input: B,
                             fResult: Future[ValidationResult],
                             editName: String,
                             uli: String,
                             validationErrorType: ValidationErrorType,
                             validationErrorEntity: ValidationErrorEntity
                           )(implicit ec: ExecutionContext): Future[HmdaValidation[B]] =
    fResult.map(result => resultToValidationError(input, result, editName, uli, validationErrorType, validationErrorEntity))

  private def resultToValidationError[B](
                                          input: B,
                                          result: ValidationResult,
                                          editName: String,
                                          uli: String,
                                          validationErrorType: ValidationErrorType,
                                          validationErrorEntity: ValidationErrorEntity
                                        ): HmdaValidation[B] =
    result match {
      case ValidationSuccess =>
        input.validNel

      case ValidationFailure =>
        validationErrorType match {
          case Syntactical =>
            SyntacticalValidationError(uli, editName, validationErrorEntity).invalidNel
          case Validity =>
            ValidityValidationError(uli, editName, validationErrorEntity).invalidNel
          case Quality =>
            QualityValidationError(uli, editName).invalidNel
          case Macro => MacroValidationError(editName).invalidNel
        }
    }
}