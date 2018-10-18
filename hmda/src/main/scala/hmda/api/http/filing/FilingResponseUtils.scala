package hmda.api.http.filing

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import hmda.api.http.model.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object FilingResponseUtils {

  def failedResponse(uri: Uri, error: Throwable) = {
    val errorResponse =
      ErrorResponse(500, error.getLocalizedMessage, uri.path)
    complete(
      ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
  }

  def entityNotPresentResponse(entityName: String,
                               id: String,
                               uri: Uri): Route = {
    val errorResponse =
      ErrorResponse(400, s"$entityName with ID: $id does not exist", uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

}
