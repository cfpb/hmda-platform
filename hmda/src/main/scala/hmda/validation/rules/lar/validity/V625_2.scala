package hmda.validation.rules.lar.validity

import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import hmda.grpc.services.{CensusServiceClient, ValidTractRequest}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.{AS, EC, MAT}
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
import hmda.validation.rules.AsyncEditCheck
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

object V625_2 extends AsyncEditCheck[LoanApplicationRegister] {

  case class TractValidate(tract: String)
  case class Tractvalidated(isValid: Boolean)
  case class CountyValidate(county: String)
  case class Countyvalidated(isValid: Boolean)

  override def name: String = "V625-2"

  val config = ConfigFactory.load()

  val host = config.getString("hmda.census.http.host")
  val port = config.getInt("hmda.census.http.port")

  override def apply[as: AS, mat: MAT, ec: EC](
      lar: LoanApplicationRegister): Future[ValidationResult] = {

    val tract = lar.geography.tract

    if (tract.toLowerCase != "na") {
      tractIsValid(tract).map {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    } else {
      Future.successful(ValidationSuccess)
    }
  }

  private def sendMessageRequest[as: AS, mat: MAT, ec: EC](
      message: TractValidate): Future[HttpRequest] = {
    val uri1 = s"http://$host:$port/census/validate/tract"
    println("Calling 625_2: " + uri1)
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
      response: HttpResponse): Future[Tractvalidated] = {
    val unmarshalledResponse = Unmarshal(response.entity)

    if (response.status == StatusCodes.OK) {
      unmarshalledResponse.to[Tractvalidated]
    } else {
      unmarshalledResponse.to[Tractvalidated]
    }
  }

  def tractIsValid[as: AS, mat: MAT, ec: EC](tract: String): Future[Boolean] = {
    val tractValidate = TractValidate(tract)
    for {
      messageRequest <- sendMessageRequest(tractValidate)
      response <- executeRequest(messageRequest)
      messageOrErrorResponse <- unmarshallResponse(response)
    } yield messageOrErrorResponse.isValid
//    val client = CensusServiceClient(
//      GrpcClientSettings.connectToServiceAt(host, port).withTls(false)
//    )
//    for {
//      response <- client
//        .validateTract(ValidTractRequest(tract))
//        .map(response => response.isValid)
//      _ <- client.close()
//      closed <- client.closed()
//    } yield (response, closed)._1
  }

}
