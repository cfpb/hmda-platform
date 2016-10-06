package hmda.api.http.institutions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi.Filing
import hmda.model.institution.Institution
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindActorByName, FindFilings }
import hmda.persistence.institutions.InstitutionPersistence.{ GetInstitutionById, GetInstitutionsById }
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait InstitutionPaths extends InstitutionProtocol with ApiErrorProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  val institutionsPath =
    path("institutions") {
      val path = "institutions"
      timedGet {
        extractRequestContext { ctx =>
          extractExecutionContext { executor =>
            implicit val ec: ExecutionContext = executor
            val ids = institutionIdsFromHeader(ctx)
            val supervisor = system.actorSelection("/user/supervisor")
            val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
            val fInstitutions = for {
              institutionsActor <- fInstitutionsActor
              institutions <- (institutionsActor ? GetInstitutionsById(ids)).mapTo[Set[Institution]]
            } yield institutions
            onComplete(fInstitutions) {
              case Success(institutions) =>
                val wrappedInstitutions = institutions.map(inst => InstitutionWrapper(inst.id.toString, inst.name, inst.status))
                complete(ToResponseMarshallable(Institutions(wrappedInstitutions)))
              case Failure(error) => completeWithInternalError(path, error)
            }
          }
        }
      }
    }

  def institutionByIdPath(institutionId: String) =
    pathEnd {
      val path = s"institutions/$institutionId"
      extractExecutionContext { executor =>
        timedGet {
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")
          val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
          val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, institutionId)).mapTo[ActorRef]
          val fInstitutionDetails = for {
            i <- fInstitutionsActor
            f <- fFilingsActor
            d <- institutionDetails(institutionId, i, f)
          } yield d

          onComplete(fInstitutionDetails) {
            case Success(institutionDetails) =>
              if (institutionDetails.institution.name != "")
                complete(ToResponseMarshallable(institutionDetails))
              else {
                val errorResponse = ErrorResponse(404, s"Institution $institutionId not found", path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              }
            case Failure(error) =>
              completeWithInternalError(path, error)
          }
        }
      }
    }

  private def institutionDetails(institutionId: String, institutionsActor: ActorRef, filingsActor: ActorRef)(implicit ec: ExecutionContext): Future[InstitutionDetail] = {
    val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[Institution]
    for {
      i <- fInstitution
      filings <- (filingsActor ? GetState).mapTo[Seq[Filing]]
    } yield InstitutionDetail(InstitutionWrapper(i.id.toString, i.name, i.status), filings)
  }
}
