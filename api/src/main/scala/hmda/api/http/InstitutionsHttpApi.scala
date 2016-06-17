package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import hmda.api.model.{ InstitutionSummary, Institutions }
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence
import hmda.api.persistence.InstitutionPersistence.GetInstitutionById
import hmda.api.protocol.processing.{ FilingProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Institution }

import scala.util.{ Failure, Success }

trait InstitutionsHttpApi extends InstitutionProtocol with FilingProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val institutionsPath =
    path("institutions") {
      val institutionsActor = system.actorSelection("/user/institutions")
      get {
        val fInstitutions = (institutionsActor ? GetState).mapTo[Set[Institution]]
        onComplete(fInstitutions) {
          case Success(institutions) =>
            complete(ToResponseMarshallable(Institutions(institutions)))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionByIdPath =
    path("institutions" / Segment) { fid =>
      val institutionsActor = system.actorSelection("/user/institutions")
      get {
        val fInstitutions = (institutionsActor ? GetInstitutionById(fid)).mapTo[Institution]
        onComplete(fInstitutions) {
          case Success(institution) =>
            complete(ToResponseMarshallable(institution))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionSummaryPath =
    path("institutions" / Segment / "summary") { fid =>
      val institutionsActor = system.actorSelection("/user/institutions")
      val filingsActor = system.actorOf(FilingPersistence.props(fid), s"filings-$fid")
      implicit val ec = system.dispatcher //TODO: customize ExecutionContext
      get {
        val fInstitution = (institutionsActor ? GetInstitutionById(fid)).mapTo[Institution]
        val fFilings = (filingsActor ? GetState).mapTo[Seq[Filing]]
        val fSummary = for {
          institution <- fInstitution
          filings <- fFilings
        } yield InstitutionSummary(institution.id, institution.name, filings)

        onComplete(fSummary) {
          case Success(summary) =>
            filingsActor ! Shutdown
            complete(ToResponseMarshallable(summary))
          case Failure(error) =>
            filingsActor ! Shutdown
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionsRoutes = institutionsPath ~ institutionByIdPath ~ institutionSummaryPath
}
