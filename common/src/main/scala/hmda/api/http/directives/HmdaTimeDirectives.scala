package hmda.api.http.directives

import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.{ Directive0, Route, RouteResult }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{ Complete, Rejected }
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }

object HmdaTimeDirectives {
  def timed(route: Route)(implicit ec: ExecutionContext): Route =
    aroundRequest(timeRequest)(ec)(route)

  private val log: Logger = LoggerFactory.getLogger(getClass)

  private val timeoutResponse = HttpResponse(StatusCodes.NetworkReadTimeout, entity = "Unable to serve response within time limit.")

  // Reference: https://blog.softwaremill.com/measuring-response-time-in-akka-http-7b6312ec70cf
  private def timeRequest(request: HttpRequest): Try[RouteResult] => Unit = {
    val start = System.currentTimeMillis()

    {
      case Success(Complete(resp)) =>
        val end          = System.currentTimeMillis()
        val responseTime = end - start
        log.info(s"[${resp.status.intValue()}] ${request.method.name} ${request.uri} took: $responseTime ms")

      case Success(Rejected(_)) =>
        log.debug("Request was rejected, not timing it")

      case Failure(_) =>
        log.debug("Request failed, not timing it")
    }
  }

  // Reference: https://blog.softwaremill.com/measuring-response-time-in-akka-http-7b6312ec70cf
  private def aroundRequest(onRequest: HttpRequest => Try[RouteResult] => Unit)(implicit ec: ExecutionContext): Directive0 =
    extractRequestContext.flatMap { ctx =>
      val onDone = onRequest(ctx.request)
      mapInnerRoute { inner =>
        withRequestTimeoutResponse { _ =>
          onDone(Success(Complete(timeoutResponse)))
          timeoutResponse
        } {
          inner.andThen { resultFuture =>
            resultFuture.map {
              case c @ Complete(response) =>
                Complete(response.mapEntity { entity =>
                  if (entity.isKnownEmpty()) {
                    onDone(Success(c))
                    entity
                  } else {
                    // On an empty entity, `transformDataBytes` unsets `isKnownEmpty`.
                    // Call onDone right away, since there's no significant amount of
                    // data to send, anyway.
                    entity.transformDataBytes(Flow[ByteString].watchTermination() {
                      case (m, f) =>
                        f.map(_ => c).onComplete(onDone)
                        m
                    })
                  }
                })
              case other =>
                onDone(Success(other))
                other
            }.andThen { // skip this if you use akka.http.scaladsl.server.handleExceptions, put onDone there
              case Failure(ex) =>
                onDone(Failure(ex))
            }
          }
        }
      }
    }
}