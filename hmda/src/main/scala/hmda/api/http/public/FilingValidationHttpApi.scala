package hmda.api.http.public

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.model.public.{ SingleValidationErrorResult, ValidationErrorSummary, ValidationSingleErrorSummary }
import hmda.model.validation._
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.model.filing.EditDescriptionLookup
import hmda.utils.YearUtils.Period
import hmda.api.http.utils.ParserErrorUtils._

trait FilingValidationHttpApi {

  def completeWithParsingErrors(lar: Option[String], errors: List[ParserValidationError]): Route = {
    complete(
      ToResponseMarshallable(StatusCodes.BadRequest -> parserValidationErrorSummaryConvertor(0, lar, errors))
    )
  }

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
