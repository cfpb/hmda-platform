package hmda.api.http

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import hmda.api.model.ErrorResponse
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

trait HmdaCustomDirectives extends ApiErrorProtocol {
  val log: LoggingAdapter

  implicit def authRejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case AuthorizationFailedRejection =>
          val errorResponse = ErrorResponse(403, "Unauthorized Access", "")
          complete(ToResponseMarshallable(StatusCodes.Forbidden -> errorResponse))
      }
      .handleNotFound {
        val errorResponse = ErrorResponse(404, "Not Found", "")
        complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
      }
      .result()

  def timedGet: Directive0 = get & time
  def timedPost: Directive0 = post & time

  def headerAuthorize: Directive0 =
    authorize(ctx =>
      hasHeader("CFPB-HMDA-Username", ctx) &&
        hasHeader("CFPB-HMDA-Institutions", ctx))

  def institutionAuthorize(institutionId: String): Directive0 = {
    authorize(ctx => institutionIdsFromHeader(ctx).contains(institutionId))
    // should this check be case insensitive?
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

  private def hasHeader(headerName: String, ctx: RequestContext): Boolean = {
    ctx.request.getHeader(headerName).isPresent
  }

  private def institutionIdsFromHeader(ctx: RequestContext): List[String] = {
    val header = ctx.request.getHeader("CFPB-HMDA-Institutions").get()
    header.value().split(",").map(_.trim).toList
  }

}
