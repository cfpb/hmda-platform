package hmda.api.http

import org.apache.pekko.http.scaladsl.server.Route
import hmda.api.HmdaServer

abstract class HttpServer extends HmdaServer {
  val paths: Route
}
