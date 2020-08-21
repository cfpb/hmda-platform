package hmda.api.http.admin

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{ Authorization, RawHeader }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Authority
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, Uri }
import akka.http.scaladsl.server.{ Directive, Directive0, Route, RouteResult }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.admin.ReplicationClient.{ InternalReplicationErrorResultCode, ResultCodeHeaderName }
import org.slf4j.LoggerFactory
import cats.syntax.all._
import cats.instances.future._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

class ReplicationClient(hostUrl: String) {

  protected val log = LoggerFactory.getLogger(this.getClass)

  // When innet route succeeds with 2xx response the same request is sent to the replication url and
  // result code is embedded in the response header
  def withRequestReplication: Directive0 =
    Directive[Unit](route =>
      extractActorSystem { implicit system =>
        extractExecutionContext { implicit ec =>
          extractRequest { originalRequest =>
            mapRouteResultWithPF({
              case RouteResult.Complete(response) if response.status.isSuccess() =>
                sendReplicatedRequest(originalRequest).map { result =>
                  val newResponse = response.addHeader(RawHeader(ResultCodeHeaderName, result.toString))
                  RouteResult.Complete(newResponse)
                }
            }) {
              route(())
            }
          }
        }
      }
    )

  type ResultCode = Int
  protected def sendReplicatedRequest(originalRequest: HttpRequest)(
    implicit ec: ExecutionContext,
    system: ActorSystem
  ): Future[ResultCode] = {
    val authHeaderOpt = originalRequest.headers.find(_.lowercaseName() == Authorization.lowercaseName)
    val request = originalRequest.copy(
      uri = originalRequest.uri.copy(authority = Authority.parse(hostUrl)),
      headers = authHeaderOpt.toList
    )
    Http()
      .singleRequest(request)
      .attempt
      .map({
        case Left(exception) =>
          log.error("Error when trying to replicate the request", exception)
          InternalReplicationErrorResultCode
        case Right(response) =>
          response.status.intValue()
      })
  }

}

object ReplicationClient {
  val ResultCodeHeaderName               = "x-hmda-replication-result-code"
  val InternalReplicationErrorResultCode = 599
}