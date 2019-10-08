package hmda.validation.engine

import cats.data.Validated
import hmda.model.validation._
import hmda.validation._
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{ AsyncEditCheck, EditCheck }

import scala.concurrent.Future

private[engine] trait ValidationEngine[A] extends ValidationApi[A] {

  def syntacticalChecks(ctx: ValidationContext): Vector[EditCheck[A]] =
    Vector.empty

  def validityChecks: Vector[EditCheck[A]] = Vector.empty

  def qualityChecks: Vector[EditCheck[A]] = Vector.empty

  def asyncChecks: Vector[AsyncEditCheck[A]] = Vector.empty

  def asyncQualityChecks: Vector[AsyncEditCheck[A]] = Vector.empty

  def checkAll(a: A, id: String, ctx: ValidationContext, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] = {
    val validations = Vector(
      checkSyntactical(a, id, ctx, validationErrorEntity),
      checkValidity(a, id, validationErrorEntity),
      checkQuality(a, id)
    )

    validations.reduceLeft(_ combine _)
  }

  def checkSyntactical(a: A, id: String, ctx: ValidationContext, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] =
    if (syntacticalChecks(ctx).isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, syntacticalChecks(ctx), Syntactical, validationErrorEntity, id)
    }

  def checkValidity(a: A, id: String, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] =
    if (validityChecks.isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, validityChecks, Validity, validationErrorEntity, id)
    }

  def checkQuality(a: A, id: String): HmdaValidation[A] =
    if (qualityChecks.isEmpty) {
      Validated.valid(a)
    } else {
      runChecks(a, qualityChecks, Quality, LarValidationError, id)
    }

  def checkValidityAsync[as: AS, mat: MAT, ec: EC](a: A, id: String): Future[HmdaValidation[A]] =
    if (asyncChecks.isEmpty) {
      Future.successful(Validated.valid(a))
    } else {
      runAsyncChecks(a, asyncChecks, Validity, LarValidationError, id)
    }

  def checkQualityAsync[as: AS, mat: MAT, ec: EC](a: A, id: String): Future[HmdaValidation[A]] =
    if (asyncQualityChecks.isEmpty) {
      Future.successful(Validated.valid(a))
    } else {
      runAsyncChecks(a, asyncQualityChecks, Quality, LarValidationError, id)
    }

  private def runChecks(a: A,
                        checksToRun: Vector[EditCheck[A]],
                        validationErrorType: ValidationErrorType,
                        validationErrorEntity: ValidationErrorEntity,
                        id: String): HmdaValidation[A] = {
    val checks =
      checksToRun
        .map(check(_, a, id, validationErrorType, validationErrorEntity))
        .toList

    checks.reduceLeft(_ combine _)
  }

  private def runAsyncChecks[as: AS, mat: MAT, ec: EC](a: A,
                                                       checksToRun: Vector[AsyncEditCheck[A]],
                                                       validationErrorType: ValidationErrorType,
                                                       validationErrorEntity: ValidationErrorEntity,
                                                       id: String): Future[HmdaValidation[A]] = {
    val fChecks =
      checksToRun
        .map(checkAsync(_, a, id, validationErrorType, validationErrorEntity))
        .toList

    val xs = Future.sequence(fChecks)
    xs.map(x => x.reduceLeft(_ combine _))
  }

}
