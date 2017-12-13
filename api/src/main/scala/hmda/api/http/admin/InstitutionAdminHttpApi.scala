package hmda.api.http.admin

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.model.fi.Filing
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindFilings
import hmda.persistence.messages.commands.filing.FilingCommands._
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence }
import hmda.persistence.messages.commands.institutions.InstitutionCommands.{ CreateInstitution, GetInstitutionById, ModifyInstitution }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName

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

  def institutionsWritePath(supervisor: ActorRef) =
    path("institutions") {
      extractExecutionContext { executor =>
        entity(as[Institution]) { institution =>
          implicit val ec: ExecutionContext = executor
          val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
          val fFilingPersistence = (supervisor ? FindFilings(FilingPersistence.name, institution.id)).mapTo[ActorRef]

          timedPost { uri =>
            val fCreated = for {
              a <- fInstitutionsActor
              i <- (a ? CreateInstitution(institution)).mapTo[Option[Institution]]
              p <- fFilingPersistence
              f <- p ? CreateFiling(Filing(institution.activityYear.toString, institution.id))
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

  def institutionReadPath(supervisor: ActorRef) =
    path("institutions" / Segment) { institutionId =>
      extractExecutionContext { executor =>
        implicit val ec: ExecutionContext = executor
        val institutionF = for {
          a <- (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
          o <- (a ? GetInstitutionById(institutionId)).mapTo[Option[Institution]]
        } yield o

        onComplete(institutionF) {
          case Success(Some(i)) =>
            complete(ToResponseMarshallable(i))
          case Success(None) =>
            complete(ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
          case Failure(_) =>
            complete(ToResponseMarshallable(HttpResponse(StatusCodes.InternalServerError)))
        }
      }
    }

  def institutionAdminRoutes(supervisor: ActorRef) = encodeResponse { institutionsWritePath(supervisor) ~ institutionReadPath(supervisor) }
}
