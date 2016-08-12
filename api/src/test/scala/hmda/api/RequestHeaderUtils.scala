package hmda.api

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import hmda.api.headers.HmdaUsernameHeader

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait RequestHeaderUtils extends RequestBuilding {
  import HttpMethods._

  def getWithCfpbHeaders(path: String): HttpRequest = {
    new RequestBuilder(GET).apply(path).addHeader(usernameHeader)
  }

  def postWithCfpbHeaders[T](path: String, content: T)(implicit m: ToEntityMarshaller[T], ec: ExecutionContext) = {
    new RequestBuilder(POST).apply(path, content).addHeader(usernameHeader)
  }

  def postWithCfpbHeaders(path: String) = {
    new RequestBuilder(POST).apply(path).addHeader(usernameHeader)
  }

  private val usernameHeader = new HmdaUsernameHeader("banker11")

}
