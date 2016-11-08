package hmda.api.http.admin

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindActorByName
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.{ CreateInstitution, GetInstitutionById, ModifyInstitution }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import hmda.api.protocol.admin.WriteInstitutionProtocol

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait InstitutionAdminHttpApi
    extends WriteInstitutionProtocol
    with ApiErrorProtocol
    with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  val institutionsWritePath =
    path("institutions") {
      extractExecutionContext { executor =>
        entity(as[Institution]) { institution =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")
          val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
          timedPost { uri =>
            val fCreated = for {
              a <- fInstitutionsActor
              i <- (a ? CreateInstitution(institution)).mapTo[Option[Institution]]
            } yield i

            onComplete(fCreated) {
              case Success(maybeCreated) =>
                maybeCreated match {
                  case Some(i) => complete(ToResponseMarshallable(StatusCodes.Created -> i))
                  case None => complete(ToResponseMarshallable(StatusCodes.Conflict))
                }
              case Failure(error) => completeWithInternalError(uri, error)

            }

          } ~
            timedPut { uri =>
              val fModified = for {
                a <- fInstitutionsActor
                m <- (a ? ModifyInstitution(institution)).mapTo[Option[Institution]]
              } yield m

              onComplete(fModified) {
                case Success(maybeInstitution) =>
                  maybeInstitution match {
                    case Some(i) => complete(ToResponseMarshallable(StatusCodes.Accepted -> i))
                    case None => complete(ToResponseMarshallable(StatusCodes.NotFound))
                  }
                case Failure(error) => completeWithInternalError(uri, error)
              }
            }
        }
      }
    }

  val institutionAdminRoutes = encodeResponse { institutionsWritePath }
}
