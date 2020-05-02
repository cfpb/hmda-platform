package hmda.validation.engine

import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import cats.data.Validated
import cats.implicits._
import hmda.model.validation._
import hmda.validation._
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{ AsyncEditCheck, EditCheck }

import scala.concurrent.{ ExecutionContext, Future }

private[engine] trait ValidationEngine[A] extends ValidationApi[A] {

  def syntacticalChecks(ctx: ValidationContext): Vector[EditCheck[A]] =
    Vector.empty

  def validityChecks(ctx: ValidationContext): Vector[EditCheck[A]] = Vector.empty

  def qualityChecks: Vector[EditCheck[A]] = Vector.empty

  def asyncChecks: Vector[AsyncEditCheck[A]] = Vector.empty

  def asyncQualityChecks: Vector[AsyncEditCheck[A]] = Vector.empty

  def checkAll(a: A, id: String, ctx: ValidationContext, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] = {
    val validations = (
      checkSyntactical(a, id, ctx, validationErrorEntity),
      checkValidity(a, id, ctx, validationErrorEntity),
      checkQuality(a, id)
      ).mapN { case (_, _, q) => q }

    validations
  }

  def checkSyntactical(a: A, id: String, ctx: ValidationContext, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] =
    if (syntacticalChecks(ctx).isEmpty) Validated.valid(a)
    else runChecks(a, syntacticalChecks(ctx), Syntactical, validationErrorEntity, id)

  def checkValidity(a: A, id: String, ctx: ValidationContext, validationErrorEntity: ValidationErrorEntity): HmdaValidation[A] =
    if (validityChecks(ctx).isEmpty) Validated.valid(a)
    else runChecks(a, validityChecks(ctx), Validity, validationErrorEntity, id)

  def checkQuality(a: A, id: String): HmdaValidation[A] =
    if (qualityChecks.isEmpty) Validated.valid(a)
    else runChecks(a, qualityChecks, Quality, LarValidationError, id)

  def checkValidityAsync(a: A, id: String)(implicit mat: Materializer, ec: ExecutionContext): Future[HmdaValidation[A]] =
    if (asyncChecks.isEmpty) Future.successful(Validated.valid(a))
    else runAsyncChecks(a, asyncChecks, Validity, LarValidationError, id)

  def checkQualityAsync(a: A, id: String)(implicit mat: Materializer, ec: ExecutionContext): Future[HmdaValidation[A]] =
    if (asyncQualityChecks.isEmpty) Future.successful(Validated.valid(a))
    else runAsyncChecks(a, asyncQualityChecks, Quality, LarValidationError, id)

  private def runChecks(
                         a: A,
                         checksToRun: Vector[EditCheck[A]],
                         validationErrorType: ValidationErrorType,
                         validationErrorEntity: ValidationErrorEntity,
                         id: String
                       ): HmdaValidation[A] = {
    val checks = checksToRun.traverse(check(_, a, id, validationErrorType, validationErrorEntity))
    checks.map(_.head)
  }

  private def runAsyncChecks(
                              a: A,
                              checksToRun: Vector[AsyncEditCheck[A]],
                              validationErrorType: ValidationErrorType,
                              validationErrorEntity: ValidationErrorEntity,
                              id: String
                            )(implicit mat: Materializer, ec: ExecutionContext): Future[HmdaValidation[A]] =
    Source(checksToRun)
      .mapAsyncUnordered(2)(checkAsync(_, a, id, validationErrorType, validationErrorEntity))
      .reduce((fst, _) => fst)
      .runWith(Sink.head)
}