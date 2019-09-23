package hmda.api.grpc

import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import hmda.api.HmdaServer

import scala.concurrent.Future

abstract class GrpcServer extends HmdaServer {

  val service: HttpRequest => Future[HttpResponse]

}
