package hmda.api.http.public

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.public.{ SingleValidationErrorResult, ValidationErrorSummary, ValidationSingleErrorSummary }
import hmda.api.http.utils.ParserErrorUtils._
import hmda.model.filing.EditDescriptionLookup
import hmda.model.validation._
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.utils.YearUtils.Period
import io.circe.generic.auto._

object FilingValidationHttpDirectives {
  def completeWithParsingErrors(lar: Option[String], errors: List[ParserValidationError]): Route =
    complete(
      BadRequest -> parserValidationErrorSummaryConvertor(0, lar, errors)
    )

  def aggregateErrors(errors: List[ValidationError], period: Period): SingleValidationErrorResult = {
    val groupedErrors = errors.groupBy(_.validationErrorType)
    def allOfType(errorType: ValidationErrorType): Seq[ValidationSingleErrorSummary] =
      groupedErrors
        .getOrElse(errorType, List())
        .map(e => ValidationSingleErrorSummary(e.editName, EditDescriptionLookup.lookupDescription(e.editName, period)))

    SingleValidationErrorResult(
      ValidationErrorSummary(allOfType(Syntactical)),
      ValidationErrorSummary(allOfType(Validity)),
      ValidationErrorSummary(allOfType(Quality))
    )
  }
}