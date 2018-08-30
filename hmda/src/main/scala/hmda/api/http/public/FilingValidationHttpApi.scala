//package hmda.api.http.public
//
//import akka.http.scaladsl.marshalling.{
//  ToResponseMarshallable,
//  ToResponseMarshaller
//}
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.server.Directives._
//import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
//import io.circe.generic.auto._
//import hmda.api.http.model.public.{
//  LarValidateResponse,
//  SingleValidationErrorResult,
//  ValidationErrorSummary
//}
//import hmda.model.validation._
//import hmda.parser.ParserErrorModel.ParserValidationError
//
//trait FilingValidationHttpApi[A] {
//
//  def completeWithParsingErrors(errors: List[ParserValidationError]): Route = {
//    val errorList = errors.map(e => e.errorMessage)
//    complete(
//      ToResponseMarshallable(
//        StatusCodes.BadRequest -> LarValidateResponse(errorList))
//    )
//  }
//
//  private def aggregateErrors(
//      errors: List[ValidationError]): SingleValidationErrorResult = {
//    val groupedErrors = errors.groupBy(_.validationErrorType)
//    def allOfType(errorType: ValidationErrorType): Seq[String] = {
//      groupedErrors.getOrElse(errorType, List()).map(e => e.editName)
//    }
//
//    SingleValidationErrorResult(
//      ValidationErrorSummary(allOfType(Syntactical)),
//      ValidationErrorSummary(allOfType(Validity)),
//      ValidationErrorSummary(allOfType(Quality))
//    )
//
//  }
//
//}
