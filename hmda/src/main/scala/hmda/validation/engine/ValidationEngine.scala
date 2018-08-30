package hmda.validation.engine

import cats.data.Validated
import hmda.model.validation.{
  Quality,
  Syntactical,
  ValidationErrorType,
  Validity
}
import hmda.validation.api.ValidationApi
import hmda.validation.rules.EditCheck

trait ValidationEngine[A] extends ValidationApi[A] {

  def syntacticalChecks: Vector[EditCheck[A]] = Vector.empty

  def validityChecks: Vector[EditCheck[A]] = Vector.empty

  def qualityChecks: Vector[EditCheck[A]] = Vector.empty

  def checkAll(a: A, id: String): HmdaValidation[A] = {
    val validations = Vector(
      checkSyntactical(a, id),
      checkValidity(a, id),
      checkQuality(a, id)
    )

    validations.par.reduceLeft(_ combine _)
  }

  def checkSyntactical(a: A, id: String): HmdaValidation[A] = {
    if (syntacticalChecks.isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, syntacticalChecks, Syntactical, id)
    }
  }

  def checkValidity(a: A, id: String): HmdaValidation[A] = {
    if (validityChecks.isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, validityChecks, Validity, id)
    }
  }

  def checkQuality(a: A, id: String): HmdaValidation[A] = {
    if (qualityChecks.isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, validityChecks, Quality, id)
    }
  }

  private def runChecks(a: A,
                        checksToRun: Vector[EditCheck[A]],
                        validationErrorType: ValidationErrorType,
                        id: String): HmdaValidation[A] = {
    val checks =
      checksToRun.par.map(check(_, a, id, validationErrorType)).toList

    checks.par.reduceLeft(_ combine _)
  }

}
