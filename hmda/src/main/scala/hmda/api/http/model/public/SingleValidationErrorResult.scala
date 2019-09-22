package hmda.api.http.model.public

import io.circe.Encoder

case class SingleValidationErrorResult(
                                        syntactical: ValidationErrorSummary = ValidationErrorSummary(Nil),
                                        validity: ValidationErrorSummary = ValidationErrorSummary(Nil),
                                        quality: ValidationErrorSummary = ValidationErrorSummary(Nil)
                                      )

object SingleValidationErrorResult {
  import io.circe.generic.auto._
  import io.circe.generic.semiauto._

  implicit val encoder: Encoder[SingleValidationErrorResult] =
    deriveEncoder[SingleValidationErrorResult]
}