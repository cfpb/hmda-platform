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

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionPaths
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
                val statusWrapper = SubmissionStatusWrapper(submission.status.code, submission.status.message)
                val submissionWrapper = SubmissionWrapper(submission.id.sequenceNumber, statusWrapper)
                complete(ToResponseMarshallable(submissionWrapper))
              }
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits
  def submissionEditsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "edits") { (period, seqNr) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val fEditChecks = getValidationState(institutionId, period, seqNr)

          val fSummaryEdits = fEditChecks.map { editChecks =>
            val s = validationErrorsToEditResults(editChecks.syntactical, Syntactical)
            val v = validationErrorsToEditResults(editChecks.validity, Validity)
            val q = validationErrorsToEditResults(editChecks.quality, Quality)
            val m = validationErrorsToEditResults(editChecks.`macro`, Macro)
            SummaryEditResults(s, v, q, m)
          }

          onComplete(fSummaryEdits) {
            case Success(edits) =>
              complete(ToResponseMarshallable(edits))
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }

    }

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits/<editType>
  def submissionSingleEditPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "edits" / Segment) { (period, seqNr, editType) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val fValidationState = getValidationState(institutionId, period, seqNr)

          val fSingleEdits = fValidationState.map { editChecks =>
            editType match {
              case "syntactical" =>
                validationErrorsToEditResults(editChecks.syntactical, Syntactical)
              case "validity" =>
                validationErrorsToEditResults(editChecks.validity, Validity)
              case "quality" =>
                validationErrorsToEditResults(editChecks.quality, Quality)
              case "macro" =>
                validationErrorsToEditResults(editChecks.`macro`, Macro)
            }
          }

          onComplete(fSingleEdits) {
            case Success(edits) =>
              complete(ToResponseMarshallable(edits))
            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }

  private def getValidationState(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[HmdaFileValidationState] = {
    val supervisor = system.actorSelection("/user/supervisor")
    val submissionID = SubmissionId(institutionId, period, seqNr)
    val fHmdaFileValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionID)).mapTo[ActorRef]

    for {
      s <- fHmdaFileValidator
      xs <- (s ? GetState).mapTo[HmdaFileValidationState]
    } yield xs
  }
}
