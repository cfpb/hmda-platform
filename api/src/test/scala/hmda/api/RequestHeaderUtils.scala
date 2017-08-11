package hmda.api

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import hmda.api.headers.{ HmdaInstitutionsHeader, HmdaUsernameHeader }

trait RequestHeaderUtils extends RequestBuilding {
  import HttpMethods._

  def getWithCfpbHeaders(path: String): HttpRequest = {
    new RequestBuilder(GET).apply(path)
      .addHeader(usernameHeader)
      .addHeader(institutionsHeader)
  }

  def postWithCfpbHeaders[T, ec: EC](path: String, content: T)(implicit m: ToEntityMarshaller[T]) = {
    new RequestBuilder(POST).apply(path, content)
      .addHeader(usernameHeader)
      .addHeader(institutionsHeader)
  }

  def postWithCfpbHeaders(path: String) = {
    new RequestBuilder(POST).apply(path)
      .addHeader(usernameHeader)
      .addHeader(institutionsHeader)
  }

  val usernameHeader = new HmdaUsernameHeader("banker11")
  val institutionsHeader = new HmdaInstitutionsHeader(List("0", "xxxxx"))

}
