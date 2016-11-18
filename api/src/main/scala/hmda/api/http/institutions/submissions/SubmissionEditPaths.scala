package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission, SubmissionId }
import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState
import hmda.query.projections.institutions.InstitutionView
import hmda.query.projections.institutions.InstitutionView.GetInstitutionById
import hmda.validation.engine.{ Macro, Quality, Syntactical, Validity }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionEditPaths
    extends InstitutionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>/edits
  def submissionEditsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "edits") { (period, seqNr) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor

          onComplete(verifyRequest(institutionId, period, seqNr)) {
            case Success(Some(message)) =>
              val errorResponse = ErrorResponse(404, message, uri.path)
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
            case Success(None) =>
              val fEditChecks = getValidationState(institutionId, period, seqNr)

              val fSummaryEdits = fEditChecks.map { editChecks =>
                val s = validationErrorsToEditResults(editChecks.tsSyntactical, editChecks.larSyntactical, Syntactical)
                val v = validationErrorsToEditResults(editChecks.tsValidity, editChecks.larValidity, Validity)
                val q = validationErrorsToEditResults(editChecks.tsQuality, editChecks.larQuality, Quality)
                val m = validationErrorsToMacroResults(editChecks.larMacro)
                SummaryEditResults(s, v, q, m)
              }

              onComplete(fSummaryEdits) {
                case Success(edits) => complete(ToResponseMarshallable(edits))
                case Failure(error) => completeWithInternalError(uri, error)
              }
            case Failure(error) => completeWithInternalError(uri, error)
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

          onComplete(verifyRequest(institutionId, period, seqNr)) {
            case Success(Some(message)) =>
              val errorResponse = ErrorResponse(404, message, uri.path)
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))

            case Success(None) =>
              val fValidationState = getValidationState(institutionId, period, seqNr)

              val fSingleEdits = fValidationState.map { editChecks =>
                editType match {
                  case "syntactical" =>
                    validationErrorsToEditResults(editChecks.tsSyntactical, editChecks.larSyntactical, Syntactical)
                  case "validity" =>
                    validationErrorsToEditResults(editChecks.tsValidity, editChecks.larValidity, Validity)
                  case "quality" =>
                    validationErrorsToEditResults(editChecks.tsQuality, editChecks.larQuality, Quality)
                  case "macro" =>
                    validationErrorsToMacroResults(editChecks.larMacro)
                }
              }

              onComplete(fSingleEdits) {
                case Success(edits: MacroResults) => complete(ToResponseMarshallable(edits))
                case Success(edits: EditResults) => complete(ToResponseMarshallable(edits))
                case Success(_) => completeWithInternalError(uri, new IllegalStateException)
                case Failure(error) => completeWithInternalError(uri, error)
              }

            case Failure(error) => completeWithInternalError(uri, error)

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

  /*
     If the request is legal (i.e. the institution, period, and filing exist), return None.
     If any of the parameters is not valid, return Some(message), where the message describes the issue.
   */
  def verifyRequest(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val submissionId = SubmissionId(institutionId, period, seqNr)

    val supervisor = system.actorSelection("/user/supervisor")
    val querySupervisor = system.actorSelection("/user/query-supervisor")

    val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, institutionId)).mapTo[ActorRef]
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

    for {
      ia <- fInstitutionsActor
      i <- (ia ? GetInstitutionById(institutionId)).mapTo[Institution]
      fa <- fFilingsActor
      f <- (fa ? GetFilingByPeriod(period)).mapTo[Filing]
      sa <- fSubmissionsActor
      s <- (sa ? GetSubmissionById(submissionId)).mapTo[Submission]
      msg <- getErrorMessage(i, f, s, institutionId, period, submissionId)
    } yield msg
  }

  private def getErrorMessage(i: Institution, f: Filing, s: Submission, iid: String, p: String, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Option[String]] = Future {
    if (s.id == sid) None
    else if (f.period == p) Some(s"Submission ${sid.sequenceNumber} not found for $p filing")
    else if (i.id == iid) Some(s"$p filing not found for institution $iid")
    else Some(s"Institution $iid not found")
  }

}
