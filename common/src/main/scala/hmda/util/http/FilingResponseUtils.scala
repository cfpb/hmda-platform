package hmda.util.http

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCode, StatusCodes, Uri }
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.model.filing.submission.SubmissionId

object FilingResponseUtils {

  def failedResponse(status: StatusCode, uri: Uri, error: Throwable): Route = {
    val errorResponse =
      ErrorResponse(status.intValue(), error.getLocalizedMessage, uri.path)
    complete(ToResponseMarshallable(status -> errorResponse))
  }

  def entityNotPresentResponse(entityName: String, id: String, uri: Uri): Route = {
    val errorResponse =
      ErrorResponse(400, s"$entityName with ID: $id does not exist", uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def entityAlreadyExists(status: StatusCode, uri: Uri, errorMessage: String): Route = {
    val errorResponse =
      ErrorResponse(status.intValue(), errorMessage, uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def entityWithLou(status: StatusCode, uri: Uri, errorMessage: String): Route = {
    val errorResponse =
      ErrorResponse(status.intValue(), errorMessage, uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def submissionNotAvailable(submissionId: SubmissionId, uri: Uri): Route = {
    val errorResponse = ErrorResponse(400, s"Submission ${submissionId.toString} not available for upload", uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def invalidTopic(status: StatusCode, topic: String, uri: Uri): Route = {
    val errorResponse = ErrorResponse(status.intValue(), s"Invalid Topic: ${topic}", uri.path)
    complete(ToResponseMarshallable(status -> errorResponse))
  }

  def badRequest(submissionId: SubmissionId, uri: Uri, reason: String): Route = {
    val errorResponse = ErrorResponse(
      400,
      reason,
      uri.path
    )
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

}