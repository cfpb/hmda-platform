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
import hmda.api.model.{ ErrorResponse, SubmissionStatusWrapper, SubmissionWrapper }
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission }
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SubmissionPaths extends InstitutionProtocol with ApiErrorProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  def submissionPath(institutionId: String) =
    path("filings" / Segment / "submissions") { period =>
      val path = s"institutions/$institutionId/filings/$period/submissions"
      extractExecutionContext { executor =>
        timedPost {
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
                    completeWithInternalError(path, error)
                }
              } else if (!filing.institutionId.isEmpty) {
                val errorResponse = ErrorResponse(404, s"$period filing not found for institution $institutionId", path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              } else {
                val errorResponse = ErrorResponse(404, s"Institution $institutionId not found", path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              }
            case Failure(error) =>
              completeWithInternalError(path, error)
          }
        }
      }
    }

  def submissionLatestPath(institutionId: String) =
    path("filings" / Segment / "submissions" / "latest") { period =>
      val path = s"institutions/$institutionId/filings/$period/submissions/latest"
      extractExecutionContext { executor =>
        timedGet {
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
                val errorResponse = ErrorResponse(404, s"No submission found for $institutionId for $period", path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
              } else {
                val statusWrapper = SubmissionStatusWrapper(submission.submissionStatus.code, submission.submissionStatus.message)
                val submissionWrapper = SubmissionWrapper(submission.id.sequenceNumber, statusWrapper)
                complete(ToResponseMarshallable(submissionWrapper))
              }
            case Failure(error) =>
              completeWithInternalError(path, error)
          }
        }
      }
    }
}
