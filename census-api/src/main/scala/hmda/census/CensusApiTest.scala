package hmda.census

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import hmda.grpc.services.{CensusServiceClient, ValidTractRequest}

import scala.concurrent.Await
import scala.concurrent.duration._

object CensusApiTest extends App {
  implicit val clientSystem = ActorSystem("CheckDigitClient")
  implicit val materializer = ActorMaterializer()
  implicit val ec = clientSystem.dispatcher

  val client = CensusServiceClient(
    GrpcClientSettings.connectToServiceAt("127.0.0.1", 60081).withTls(false))

  val replyF =
    client.validateTract(ValidTractRequest("test"))
//      validateUli(ValidUliRequest("10Cx939c5543TqA1144M999143X10"))

  val result = Await.result(replyF, 2.seconds)
  println("ULI IS VALID?: " + result)

}
