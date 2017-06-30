package hmda.api.http.institutions

import java.time.Instant

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Framing, Sink }
import akka.util.{ ByteString, Timeout }
import hmda.api.EC
import hmda.api.http.HmdaCustomDirectives
import hmda.persistence.messages.CommonMessages._
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.{ Created, Failed, Submission, SubmissionId, Uploaded }
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.HmdaRawFile.{ AddFileName, AddLine }
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, Persisted, StartUpload }
import hmda.persistence.processing.SubmissionManager
import hmda.query.HmdaQuerySupervisor.FindHmdaFilingView
import hmda.query.projections.filing.HmdaFilingDBProjection.{ CreateSchema, DeleteLars }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait UploadPaths extends InstitutionProtocol with ApiErrorProtocol with SubmissionProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  implicit val flowParallelism: Int

  val splitLines = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>
  def uploadPath[_: EC](institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber) { (period, seqNr) =>
      timedPost { uri =>
        val submissionId = SubmissionId(institutionId, period, seqNr)
        val uploadTimestamp = Instant.now.toEpochMilli
        val supervisor = system.actorSelection("/user/supervisor")
        val querySupervisor = system.actorSelection("/user/query-supervisor")
        val fProcessingActor = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
        val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]
        (supervisor ? FindHmdaFilingView(period)).mapTo[ActorRef]
        (querySupervisor ? FindHmdaFilingView(period)).mapTo[ActorRef]

        val fUploadSubmission = for {
          p <- fProcessingActor
          s <- fSubmissionsActor
          fSubmission <- (s ? GetSubmissionById(submissionId)).mapTo[Submission]
        } yield (fSubmission, fSubmission.status == Created, p)

        onComplete(fUploadSubmission) {
          case Success((submission, true, processingActor)) =>
            val queryProjector = system.actorSelection(s"/user/query-supervisor/HmdaFilingView-$period/queryProjector")
            queryProjector ! CreateSchema
            queryProjector ! DeleteLars(institutionId)
            uploadFile(processingActor, uploadTimestamp, uri.path, submission)
          case Success((_, false, _)) =>
            val errorResponse = Failed(s"Submission $seqNr not available for upload")
            complete(ToResponseMarshallable(StatusCodes.BadRequest -> Submission(submissionId, errorResponse, 0L, 0L)))
          case Failure(error) =>
            completeWithInternalError(uri, error)
        }
      }
    }

  private def uploadFile(processingActor: ActorRef, uploadTimestamp: Long, path: Path, submission: Submission): Route = {
    fileUpload("file") {
      case (metadata, byteSource) if metadata.fileName.endsWith(".txt") =>
        processingActor ! AddFileName(metadata.fileName)
        processingActor ! StartUpload
        val uploadedF = byteSource
          .via(splitLines)
          .map(_.utf8String)
          .mapAsync(parallelism = flowParallelism)(line => (processingActor ? AddLine(uploadTimestamp, line)).mapTo[Persisted.type])
          .runWith(Sink.ignore)

        onComplete(uploadedF) {
          case Success(_) =>
            processingActor ! CompleteUpload
            complete(ToResponseMarshallable(StatusCodes.Accepted -> submission.copy(status = Uploaded)))
          case Failure(error) =>
            processingActor ! Shutdown
            log.error(error.getLocalizedMessage)
            val errorResponse = Failed("Invalid File Format")
            complete(ToResponseMarshallable(StatusCodes.BadRequest -> Submission(submission.id, errorResponse)))
        }
      case _ =>
        processingActor ! Shutdown
        val errorResponse = Failed("Invalid File Format")
        complete(ToResponseMarshallable(StatusCodes.BadRequest -> Submission(submission.id, errorResponse)))
    }
  }
}
