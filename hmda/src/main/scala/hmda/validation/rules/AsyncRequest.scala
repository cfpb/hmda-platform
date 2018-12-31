package hmda.validation.rules

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hmda.validation.{AS, EC, MAT}

import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.validation.model.AsyncModel._
import io.circe.generic.auto._
trait AsyncRequest {

  def sendMessageRequestTract[as: AS, mat: MAT, ec: EC](
      message: TractValidate,
      host: String,
      port: Int,
      uri: String): Future[HttpRequest] = {
    Marshal(message).to[RequestEntity].map { entity =>
      HttpRequest(
        method = HttpMethods.POST,
        uri = s"http://$host:$port$uri",
        entity = entity.withContentType(ContentTypes.`application/json`)
      )
    }
  }

  def sendMessageRequestCounty[as: AS, mat: MAT, ec: EC](
      message: CountyValidate,
      host: String,
      port: Int,
      uri: String): Future[HttpRequest] = {
    Marshal(message).to[RequestEntity].map { entity =>
      HttpRequest(
        method = HttpMethods.POST,
        uri = s"http://$host:$port$uri",
        entity = entity.withContentType(ContentTypes.`application/json`)
      )
    }
  }

  def sendMessageRequestUli[as: AS, mat: MAT, ec: EC](
      message: ULIValidate,
      host: String,
      port: Int,
      uri: String): Future[HttpRequest] = {
    Marshal(message).to[RequestEntity].map { entity =>
      HttpRequest(
        method = HttpMethods.POST,
        uri = s"http://$host:$port$uri",
        entity = entity
      )
    }
  }

  def executeRequest[as: AS, mat: MAT, ec: EC](
      httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest)
  }

  def unmarshallResponse[as: AS, mat: MAT, ec: EC](response: HttpResponse,
                                                   responseType: String) = {
    val unmarshalledResponse = Unmarshal(response.entity)

    responseType match {
      case "uli" =>
        if (response.status == StatusCodes.OK) {
          unmarshalledResponse.to[ULIValidated]
        } else {
          unmarshalledResponse.to[ULIValidated]
        }
      case "county" =>
        if (response.status == StatusCodes.OK) {
          unmarshalledResponse.to[Countyvalidated]
        } else {
          unmarshalledResponse.to[Countyvalidated]
        }
      case "tract" =>
        if (response.status == StatusCodes.OK) {
          unmarshalledResponse.to[Tractvalidated]
        } else {
          unmarshalledResponse.to[Tractvalidated]
        }
    }
  }
}
