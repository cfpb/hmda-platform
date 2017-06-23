package hmda.api.http.admin

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.apiModel.protocol.admin.WriteInstitutionProtocol
import hmda.apiModel.protocol.processing.ApiErrorProtocol
import hmda.model.fi.Filing
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindFilings
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence }
import hmda.persistence.institutions.InstitutionPersistence.{ CreateInstitution, ModifyInstitution }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.projections.institutions.InstitutionDBProjection.{ CreateSchema, DeleteSchema }
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex
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

  private val cdRegex = new Regex("create|delete")
  val institutionsSchemaPath =
    path("institutions" / cdRegex) { command =>
      extractExecutionContext { executor =>
        implicit val ec: ExecutionContext = executor
        val querySupervisor = system.actorSelection("/user/query-supervisor")
        val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
        val message = command match {
          case "create" => CreateSchema
          case "delete" => DeleteSchema
        }
        timedGet { uri =>
          val event = for {
            instAct <- fInstitutionsActor
            dbAct <- (instAct ? GetProjectionActorRef).mapTo[ActorRef]
            cd <- (dbAct ? message).mapTo[Event]
          } yield cd

          onComplete(event) {
            case Success(response) => complete(ToResponseMarshallable(StatusCodes.Accepted -> response.toString))
            case Failure(error) => completeWithInternalError(uri, error)
          }
        }
      }
    }

  val institutionAdminRoutes = encodeResponse { institutionsWritePath ~ institutionsSchemaPath }
}
