package hmda.validation.rules.lar.validity

import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import com.typesafe.config.ConfigFactory
import hmda.grpc.services.{CheckDigitServiceClient, ValidUliRequest}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.AsyncEditCheck
import akka.http.scaladsl.marshalling.{Marshal, ToResponseMarshallable}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{
  Accept,
  Authorization,
  OAuth2BearerToken
}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future

object V609 extends AsyncEditCheck[LoanApplicationRegister] {

  case class ULIValidate(uli: String)
  case class ULIValidated(isValid: Boolean)
  override def name: String = "V609"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.uli.grpc.host")
  val port = config.getInt("hmda.uli.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val uli = lar.loan.ULI

    if (uli.length <= 22) {
      Future.successful(ValidationSuccess)
    } else {
      uliIsValid(uli).map {
        case true =>
          println("IT WAS TRUEEEE")
          ValidationSuccess
        case false =>
          println("IT WAS FALSEEE")
          ValidationFailure
      }
    }

  }

  private def sendMessageRequest[as: AS, mat: MAT, ec: EC](
      message: ULIValidate): Future[HttpRequest] = {
    val uri1 = s"http://$host:$port/uli/validate"
    println("Calling V609: " + uri1)
    println(uri1)
    Marshal(message).to[RequestEntity].map { entity =>
      HttpRequest(
        method = HttpMethods.POST,
        uri = uri1,
        entity = entity
      )
    }
  }
  protected def executeRequest[as: AS, mat: MAT, ec: EC](
      httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest)
  }

  private def unmarshallResponse[as: AS, mat: MAT, ec: EC](
      response: HttpResponse): Future[ULIValidated] = {
    val unmarshalledResponse = Unmarshal(response.entity)

    if (response.status == StatusCodes.OK) {
      unmarshalledResponse.to[ULIValidated]
    } else {
      unmarshalledResponse.to[ULIValidated]
    }
  }

  def uliIsValid[as: AS, mat: MAT, ec: EC](uli: String): Future[Boolean] = {

    val uliValidate = ULIValidate(uli)
    for {

      messageRequest <- sendMessageRequest(uliValidate)
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response)
    } yield messageOrErrorResponse.isValid
    //    val responseFuture: Future[HttpResponse] = Http().singleRequest(uliValidate)
    //    val client = CheckDigitServiceClient(
    //      GrpcClientSettings.connectToServiceAt(host, port).withTls(false)
    //    )
    //    for {
    //      response <- client
    //        .validateUli(ValidUliRequest(uli))
    //        .map(response => response.isValid)
    //      _ <- client.close()
    //      closed <- client.closed()
    //    } yield (response, closed)._1
  }

}
