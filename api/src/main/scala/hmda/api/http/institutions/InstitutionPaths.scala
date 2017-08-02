package hmda.api.http.institutions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.EC
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi.Filing
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindFilings
import hmda.persistence.institutions.FilingPersistence
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.institutions.InstitutionView.{ GetInstitutionById, GetInstitutionsById }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait InstitutionPaths extends InstitutionProtocol with ApiErrorProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  // institutions
  def institutionsPath[_: EC] =
    path("institutions") {
      timedGet { uri =>
        extractRequestContext { ctx =>
          val ids = institutionIdsFromHeader(ctx)
          val supervisor = system.actorSelection("/user/query-supervisor")
          val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
          val fInstitutions = for {
            institutionsActor <- fInstitutionsActor
            institutions <- (institutionsActor ? GetInstitutionsById(ids)).mapTo[Set[Institution]]
          } yield institutions
          onComplete(fInstitutions) {
            case Success(institutions) =>
              val wrappedInstitutions = institutions.map(inst => InstitutionWrapper(inst.id, inst.respondent.name))
              complete(ToResponseMarshallable(Institutions(wrappedInstitutions)))
            case Failure(error) => completeWithInternalError(uri, error)
          }
        }
      }
    }

  // institutions/<institutionId>
  def institutionByIdPath[_: EC](institutionId: String) =
    pathEnd {
      timedGet { uri =>
        val supervisor = system.actorSelection("/user/supervisor")
        val querySupervisor = system.actorSelection("/user/query-supervisor")
        val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
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
              val errorResponse = ErrorResponse(404, s"Institution $institutionId not found", uri.path)
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
            }
          case Failure(error) =>
            completeWithInternalError(uri, error)
        }
      }
    }

  private def institutionDetails[_: EC](institutionId: String, institutionsActor: ActorRef, filingsActor: ActorRef): Future[InstitutionDetail] = {
    val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[Institution]
    for {
      i <- fInstitution
      filings <- (filingsActor ? GetState).mapTo[Seq[Filing]]
    } yield InstitutionDetail(InstitutionWrapper(i.id, i.respondent.name), filings)
  }
}
