package hmda.quarterly.data.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import hmda.quarterly.data.model.QuarterGraphData._
import spray.json.DefaultJsonProtocol

import java.time.LocalDateTime
import scala.util.Random

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coordinateFormat = jsonFormat2(GraphCoordinate)
  implicit val graphData = jsonFormat3(GraphSummary)
}

object HmdaQuarterlyDataRequestHandler extends JsonSupport {

  val routes: Route = {
    pathPrefix("graph") {
      path(Segment) { graph =>
        parameters("start".withDefault(""), "end".withDefault("")) { (start, end) =>
          complete(GraphSummary(graph, LocalDateTime.now().toString, Seq(GraphCoordinate("test", "123"))))
        }
      }
    }
  }
}
