package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.ValidationErrorConverter
import hmda.api.model.ErrorResponse
import hmda.api.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.SubmissionId
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Agency
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.processing.{ HmdaFileValidator, HmdaRawFile, SubmissionManager }
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.SubmissionManager.GetActorRef
import hmda.persistence.processing.HmdaRawFile.HmdaRawFileState

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }

trait SubmissionSummaryPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with RequestVerificationUtils
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  case class TsLarSummary(ts: Option[TransmittalSheet], larSize: Int, hmdaFileName: String)

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/summary
  def submissionSummaryPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "summary") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(institutionId, period, seqNr, uri) {
          val submissionId = SubmissionId(institutionId, period, seqNr)

          val supervisor = system.actorSelection("/user/supervisor")
          val submissionManagerF = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
          val validatorF = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
          val hmdaRawF = submissionManagerF.flatMap(actorRef => (actorRef ? GetActorRef(HmdaRawFile.name)).mapTo[ActorRef])

          val tsF = for {
            validator <- validatorF
            hmdaRaw <- hmdaRawF
            u <- (hmdaRaw ? GetState).mapTo[HmdaRawFileState]
            s <- (validator ? GetState).mapTo[HmdaFileValidationState]
            fileDetails = u.fileName
            larSize = s.lars.size
            ts = s.ts
            tsLarSummary = TsLarSummary(ts, larSize, fileDetails)
          } yield tsLarSummary

          onComplete(tsF) {
            case Success(x) => x.ts match {
              case Some(t) =>
                val contactSummary = ContactSummary(t.contact.name, t.contact.phone, t.contact.email)
                val agency = Try(Agency.withValue(t.agencyCode)).getOrElse(Agency.UndeterminedAgency)
                val respondentSummary = RespondentSummary(t.respondent.name, t.respondent.id, t.taxId, agency.name, contactSummary)

                val fileSummary = FileSummary(x.hmdaFileName, t.activityYear.toString, x.larSize)
                val submissionSummary = SubmissionSummary(respondentSummary, fileSummary)
                complete(ToResponseMarshallable(submissionSummary))
              case None =>
                val errorResponse = ErrorResponse(404, s"submission $submissionId not found", uri.path)
                complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
            }

            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }
}
