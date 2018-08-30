package hmda.validation.engine

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

  def checkAll(a: A): HmdaValidation[A]

  def checkSyntactical(a: A, id: String): HmdaValidation[A] = {
    runChecks(a, syntacticalChecks, Syntactical, id)
  }

  def checkValidity(a: A, id: String): HmdaValidation[A] = {
    runChecks(a, validityChecks, Validity, id)
  }

  def checkQuality(a: A, id: String): HmdaValidation[A] = {
    runChecks(a, validityChecks, Quality, id)
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
