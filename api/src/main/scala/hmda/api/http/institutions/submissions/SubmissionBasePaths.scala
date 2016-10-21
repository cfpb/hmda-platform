package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission, SubmissionId }
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.validation.engine.{ Macro, Quality, Syntactical, Validity }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SubmissionBasePaths
    extends InstitutionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions
  def submissionPath(institutionId: String) =
    path("filings" / Segment / "submissions") { period =>
      extractExecutionContext { executor =>
        timedPost { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")
          val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, institutionId)).mapTo[ActorRef]
          val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

          val fFiling = for {
            f <- fFilingsActor
            s <- fSubmissionsActor
            d <- (f ? GetFilingByPeriod(period)).mapTo[Filing]
          } yield (s, d)

          onComplete(fFiling) {
            case Success((submissionsActor, filing)) =>
              if (filing.period == period) {
                submissionsActor ! CreateSubmission
                val fLatest = (submissionsActor ? GetLatestSubmission).mapTo[Submission]
                onComplete(fLatest) {
                  case Success(submission) =>
                    complete(ToResponseMarshallable(StatusCodes.Created -> submission))
                  case Failure(error) =>
                    completeWithInternalError(uri, error)
                }
              } else if (!filing.institutionId.isEmpty) {
                val errorResponse = ErrorResponse(404, s"$period filing not found for institution $institutionId", uri.path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              } else {
                val errorResponse = ErrorResponse(404, s"Institution $institutionId not found", uri.path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              }
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/latest
  def submissionLatestPath(institutionId: String) =
    path("filings" / Segment / "submissions" / "latest") { period =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")
          val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

          val fSubmissions = for {
            s <- fSubmissionsActor
            xs <- (s ? GetLatestSubmission).mapTo[Submission]
          } yield xs

          onComplete(fSubmissions) {
            case Success(submission) =>
              if (submission.id.sequenceNumber == 0) {
                val errorResponse = ErrorResponse(404, s"No submission found for $institutionId for $period", uri.path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              } else {
                complete(ToResponseMarshallable(submission))
              }
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }
}
