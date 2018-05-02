package hmda.api.http.admin

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.api.protocol.public.HmdaFilerProtocol
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import hmda.model.institution.HmdaFiler
import hmda.persistence.HmdaSupervisor.FindHmdaFilerPersistence
import hmda.persistence.institutions.HmdaFilerPersistence
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait HmdaFilerAdminHttpApi extends HmdaFilerProtocol with HmdaCustomDirectives with ApiErrorProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  def hmdaFilerPath(supervisor: ActorRef) = {
    path("filers") {
      timedGet { uri =>
        extractExecutionContext { executor =>
          implicit val ec: ExecutionContext = executor
          val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
          val filersF = for {
            a <- hmdaFilerPersistenceF
            filers <- (a ? GetState).mapTo[Set[HmdaFiler]]
          } yield filers

          onComplete(filersF) {
            case Success(filers) =>
              complete(ToResponseMarshallable(filers))
            case Failure(e) =>
              completeWithInternalError(uri, e)
          }
        }
      } ~
        timedPost { uri =>
          extractExecutionContext { executor =>
            entity(as[HmdaFiler]) { hmdaFiler =>
              implicit val ec: ExecutionContext = executor
              val fHmdaFilerPersistence = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
              val fCreated = for {
                a <- fHmdaFilerPersistence
                h <- (a ? CreateHmdaFiler(hmdaFiler)).mapTo[HmdaFilerCreated]
              } yield h

              onComplete(fCreated) {
                case Success(filerCreated) => complete(ToResponseMarshallable(StatusCodes.Created -> filerCreated.hmdFiler))
                case Failure(error) => completeWithInternalError(uri, error)
              }
            }
          }
        } ~
        timedDelete { uri =>
          extractExecutionContext { executor =>
            entity(as[HmdaFiler]) { hmdaFiler =>
              implicit val ec: ExecutionContext = executor
              val fHmdaFilerPersistence = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
              val fDeleted = for {
                a <- fHmdaFilerPersistence
                h <- (a ? DeleteHmdaFiler(hmdaFiler)).mapTo[Option[HmdaFilerDeleted]]
              } yield h

              onComplete(fDeleted) {
                case Success(maybeFiler) => maybeFiler match {
                  case Some(filer) => complete(ToResponseMarshallable(StatusCodes.Accepted -> filer.hmdaFiler))
                  case None => complete(StatusCodes.NotFound)
                }

                case Failure(error) => completeWithInternalError(uri, error)
              }
            }
          }
        }

    }
  }

  def hmdaFilerAdminRoutes(supervisor: ActorRef) = encodeResponse {
    hmdaFilerPath(supervisor)
  }

}
