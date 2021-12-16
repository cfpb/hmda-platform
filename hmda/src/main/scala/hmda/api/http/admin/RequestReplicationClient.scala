package hmda.api.http.admin
// $COVERAGE-OFF$
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{Authorization, RawHeader}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.server.{Directive, Directive0, Directives, RouteResult}
import akka.http.scaladsl.server.Directives._
import hmda.api.http.admin.RequestReplicationClient.{InternalReplicationErrorResultCode, ResultCodeHeaderName}
import org.slf4j.LoggerFactory
import cats.syntax.all._
import cats.instances.future._
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

trait RequestReplicationClient {
  def withRequestReplication: Directive0
}

object RequestReplicationClient {
  val ResultCodeHeaderName               = "x-hmda-replication-result-code"
  val InternalReplicationErrorResultCode = 599
  private val log = LoggerFactory.getLogger(this.getClass)

  def create(config: Config, key: String): RequestReplicationClient = {
    val address = config.getString(key)
    if(address.isEmpty){
      log.debug(s"No replication address defined under $key, requests replication will be disabled")
      NoopRequestReplicationClient$
    } else {
      log.debug(s"Enabling request replication for $key to $address")
      new RealRequestReplicationClient(address)
    }
  }


}

class RealRequestReplicationClient(replicationAddress: String) extends RequestReplicationClient {

  protected val log = LoggerFactory.getLogger(this.getClass)
  protected val replicationUri = Uri(replicationAddress)

  // When inner route succeeds with 2xx response the same request is sent to the replication url and
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
      uri = originalRequest.uri.copy(
        scheme = replicationUri.scheme,
        authority = replicationUri.authority,
        path = replicationUri.path ++ originalRequest.uri.path
      ),
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
          response.entity.discardBytes()
          response.status.intValue()
      })
  }

}

object NoopRequestReplicationClient$ extends RequestReplicationClient {
  override def withRequestReplication: Directive0 = Directives.pass
}
// $COVERAGE-ON$