package hmda.loader.http

import akka.NotUsed
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.parser.fi.InstitutionParser
import spray.json._

trait InstitutionFlowUtils extends WriteInstitutionProtocol {

  def institutionStringToHttpFlow(url: Uri): Flow[String, HttpRequest, NotUsed] =
    Flow[String]
      .map { x =>
        val payload = ByteString(InstitutionParser(x).toJson.toString)
        HttpRequest(
          HttpMethods.POST,
          uri = url,
          entity = HttpEntity(MediaTypes.`application/json`, payload)
        )
      }
}
