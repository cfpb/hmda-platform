package hmda.validation.rules

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import hmda.validation.{AS, EC, MAT}
import hmda.validation.model.{RequestMessage, ResponseMessage}
import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
trait AsyncRequest {

  def sendMessageRequest[as: AS, mat: MAT, ec: EC](
      message: RequestMessage,
      host: String,
      port: Int): Future[HttpRequest] = {
    Marshal(message).to[RequestEntity].map { entity =>
      HttpRequest(
        method = HttpMethods.POST,
        uri = s"http://$host:$port/uli/validate",
        entity = entity
      )
    }
  }

  def executeRequest[as: AS, mat: MAT, ec: EC](
      httpRequest: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(httpRequest)
  }

  def unmarshallResponse[as: AS, mat: MAT, ec: EC](
      response: HttpResponse): Future[ResponseMessage] = {
    val unmarshalledResponse = Unmarshal(response.entity)

    if (response.status == StatusCodes.OK) {
      unmarshalledResponse.to[ResponseMessage]
    } else {
      unmarshalledResponse.to[ResponseMessage]
    }
  }

}
