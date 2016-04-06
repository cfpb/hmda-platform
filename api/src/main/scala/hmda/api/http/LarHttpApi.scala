package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import hmda.api.protocol.validation.ValidationResultProtocol
import hmda.parser.fi.lar.LarCsvParser
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.protocol.fi.lar.LarProtocol

trait LarHttpApi extends LarProtocol with ValidationResultProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val parseLarRoute =
    pathPrefix("lar") {
      path("parse") {
        post {
          entity(as[String]) { s =>
            val lar = LarCsvParser(s)
            //TODO: return human readable errors when parser fails. See issue #62
            complete {
              ToResponseMarshallable(lar)
            }
          }
        }
      }
    }

  val larRoutes = parseLarRoute

}
