package hmda.api.http

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import hmda.api.model.ErrorResponse
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

trait HmdaCustomDirectives extends ApiErrorProtocol {
  val log: LoggingAdapter

  implicit def authRejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          extractUri { uri =>
            val errorResponse = ErrorResponse(403, "Unauthorized Access", uri.path)
            complete(ToResponseMarshallable(StatusCodes.Forbidden -> errorResponse))
          }
      }
      .handleNotFound {
        extractUri { uri =>
          val errorResponse = ErrorResponse(404, "Not Found", uri.path)
          complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
        }
      }
      .result()

  def timedGet = get & time & extractUri
  def timedPost = post & time & extractUri
  def timedPut = put & time & extractUri

  def headerAuthorize: Directive0 =
    authorize(ctx =>
      hasHeader("CFPB-HMDA-Username", ctx) &&
        hasHeader("CFPB-HMDA-Institutions", ctx))

  def institutionAuthorize(institutionId: String): Directive0 = {
    authorize { ctx =>
      institutionIdsFromHeader(ctx).map(_.toLowerCase)
        .contains(institutionId.toLowerCase)
    }
  }

  def institutionIdsFromHeader(ctx: RequestContext): List[String] = {
    val header = ctx.request.getHeader("CFPB-HMDA-Institutions").get()
    header.value().split(",").map(_.trim).toList
  }

  def time: Directive0 = {
    val startTime = System.currentTimeMillis()

    mapResponse { response =>
      val endTime = System.currentTimeMillis()
      val responseTime = endTime - startTime
      log.debug(s"Request took $responseTime ms")
      response
    }

  }

  def completeWithInternalError(uri: Uri, error: Throwable): StandardRoute = {
    log.error(error.getLocalizedMessage)
    val errorResponse = ErrorResponse(500, "Internal server error", uri.path)
    complete(ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
  }

  private def hasHeader(headerName: String, ctx: RequestContext): Boolean = {
    ctx.request.getHeader(headerName).isPresent
  }

}
