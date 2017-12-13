package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import hmda.api.http.ValidationErrorConverter
import hmda.api.model.ErrorResponse
import hmda.api.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Agency
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.processing.HmdaFileValidator
import hmda.persistence.processing.HmdaFileValidator._
import hmda.validation.ValidationStats.FindTotalSubmittedLars
import hmda.validation.rules.StatsLookup

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }

trait SubmissionSummaryPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with RequestVerificationUtils
    with ValidationErrorConverter
    with StatsLookup {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  case class TsLarSummary(ts: Option[TransmittalSheet], larSize: Int, hmdaFileName: String)

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/summary
  def submissionSummaryPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "summary") { (period, seqNr) =>
      timedGet { uri =>
        completeVerified(supervisor, institutionId, period, seqNr, uri) {
          val submissionId = SubmissionId(institutionId, period, seqNr)
          val validatorF = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
          val submissionPersistenceF = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]

          val tsF = for {
            validator <- validatorF
            submissions <- submissionPersistenceF
            ts <- (validator ? GetTs).mapTo[Option[TransmittalSheet]]
            sub <- (submissions ? GetSubmissionById(submissionId)).mapTo[Submission]
            actorRef <- validationStats
            totalLars <- (actorRef ? FindTotalSubmittedLars(sub.id.institutionId, period)).mapTo[Int]
          } yield TsLarSummary(ts, totalLars, sub.fileName)

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
