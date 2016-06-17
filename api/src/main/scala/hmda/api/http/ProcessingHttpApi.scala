package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.model.processing.Institutions
import hmda.api.processing.CommonMessages.GetState
import hmda.api.protocol.processing.ProcessingProtocol

import scala.util.{ Failure, Success }

trait ProcessingHttpApi extends ProcessingProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val institutionsPath =
    pathPrefix("institutions") {
      path("period" / Segment) { period =>
        get {
          val institutionsActor = system.actorSelection("/user/institutions")
          val fList = (institutionsActor ? GetState)
            .mapTo[Institutions]

          onComplete(fList) {
            case Success(xs) =>
              val institutions = xs.institutions
              val filtered = institutions.filter(i => i.period == period)
              complete(ToResponseMarshallable(Institutions(filtered)))
            case Failure(error) => complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }

  val processingRoutes = institutionsPath
}
