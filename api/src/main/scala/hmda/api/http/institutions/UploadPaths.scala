package hmda.api.http.institutions

import java.time.Instant

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Framing
import akka.util.{ ByteString, Timeout }
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.ErrorResponse
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi.{ Created, Submission, SubmissionId }
import hmda.persistence.CommonMessages._
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.HmdaRawFile
import hmda.persistence.processing.HmdaRawFile.{ AddLine, CompleteUpload, StartUpload }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait UploadPaths extends InstitutionProtocol with ApiErrorProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val splitLines = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

  def uploadPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber) { (period, seqNr) =>
      time {
        val path = s"institutions/$institutionId/filings/$period/submissions/$seqNr"
        val submissionId = SubmissionId(institutionId, period, seqNr)
        extractExecutionContext { executor =>
          implicit val ec: ExecutionContext = executor
          val uploadTimestamp = Instant.now.toEpochMilli
          val supervisor = system.actorSelection("/user/supervisor")
          val fProcessingActor = (supervisor ? FindProcessingActor(HmdaRawFile.name, submissionId)).mapTo[ActorRef]
          val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

          val fUploadSubmission = for {
            p <- fProcessingActor
            s <- fSubmissionsActor
            fIsSubmissionOverwrite <- checkSubmissionOverwrite(s, submissionId)
          } yield (fIsSubmissionOverwrite, p)

          onComplete(fUploadSubmission) {
            case Success((false, processingActor)) =>
              processingActor ! StartUpload
              uploadFile(processingActor, uploadTimestamp, path)
            case Success((true, _)) =>
              val errorResponse = ErrorResponse(400, s"Submission $seqNr not available for upload", path)
              complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
            case Failure(error) =>
              completeWithInternalError(path, error)
          }
        }
      }
    }

  private def checkSubmissionOverwrite(submissionsActor: ActorRef, submissionId: SubmissionId)(implicit ec: ExecutionContext): Future[Boolean] = {
    val submission = (submissionsActor ? GetSubmissionById(submissionId)).mapTo[Submission]
    submission.map(_.submissionStatus != Created)
  }

  private def uploadFile(processingActor: ActorRef, uploadTimestamp: Long, path: String): Route = {
    fileUpload("file") {
      case (metadata, byteSource) if (metadata.fileName.endsWith(".txt")) =>
        val uploadedF = byteSource
          .via(splitLines)
          .map(_.utf8String)
          .runForeach(line => processingActor ! AddLine(uploadTimestamp, line))

        onComplete(uploadedF) {
          case Success(response) =>
            processingActor ! CompleteUpload
            processingActor ! Shutdown
            complete(ToResponseMarshallable(StatusCodes.Accepted -> "uploaded"))
          case Failure(error) =>
            processingActor ! Shutdown
            log.error(error.getLocalizedMessage)
            val errorResponse = ErrorResponse(400, "Invalid File Format", path)
            complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
        }
      case _ =>
        processingActor ! Shutdown
        val errorResponse = ErrorResponse(400, "Invalid File Format", path)
        complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
    }
  }
}
