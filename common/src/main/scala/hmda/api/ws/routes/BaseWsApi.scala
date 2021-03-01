package hmda.api.ws.routes

import java.net.InetAddress
import java.time.Instant

import akka.NotUsed
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives.{ get, handleWebSocketMessages, pathSingleSlash }
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import hmda.api.http.model.HmdaServiceStatus
import io.circe.generic.auto._
import io.circe.syntax._
import hmda.BuildInfo
object BaseWsApi {
  def route(apiName: String): Route = cors() {
    rootPath(apiName)
  }

  private def rootPath(name: String): Route =
    pathSingleSlash {
      get {
        handleWebSocketMessages(baseHandler(name))
      }
    }

  private def baseHandler(name: String): Flow[Message, Message, NotUsed] =
    Flow[Message].map {
      case TextMessage.Strict(txt) =>
        txt match {
          case "status" =>
            val now    = Instant.now.toString
            val host   = InetAddress.getLocalHost.getHostName
            val status = HmdaServiceStatus("OK", name, now, host, BuildInfo.latestGitTag)
            TextMessage.Strict(status.asJson.toString)

          case _ => TextMessage.Strict("Message not supported")
        }
      case _ => TextMessage.Strict("Message not supported")
    }
}