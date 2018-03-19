package hmda.api.http.public

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.model.public.HmdaFilerResponse
import hmda.api.protocol.public.HmdaFilerProtocol
import hmda.model.institution.HmdaFiler
import hmda.persistence.HmdaSupervisor.FindHmdaFilerPersistence
import hmda.persistence.institutions.HmdaFilerPersistence
import hmda.persistence.messages.CommonMessages.GetState

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait HmdaFilerPaths extends HmdaCustomDirectives with HmdaFilerProtocol with ApiErrorProtocol {

  implicit val timeout: Timeout
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  def hmdaFilersPath(supervisor: ActorRef) = {
    path("filers") {
      encodeResponse {
        timedGet { uri =>
          val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
          val filersF = for {
            a <- hmdaFilerPersistenceF
            filers <- (a ? GetState).mapTo[Set[HmdaFiler]]
          } yield filers

          onComplete(filersF) {
            case Success(filers) =>
              complete(ToResponseMarshallable(HmdaFilerResponse(filers)))
            case Failure(e) =>
              completeWithInternalError(uri, e)
          }
        }
      }
    }
  }

}
