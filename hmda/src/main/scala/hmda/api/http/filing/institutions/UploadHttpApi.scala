package hmda.api.http.filing.institutions

import java.time.Instant

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Sink}
import akka.util.{ByteString, Timeout}
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.model.ErrorResponse
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.model.filing.submission._
import hmda.persistence.submission.SubmissionPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait UploadHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val log: LoggingAdapter
  val sharding: ClusterSharding
  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>
  def uploadHmdaFileRoute: Route =
    path(Segment / "filings" / Segment / "submissions" / IntNumber) {
      (lei, period, seqNr) =>
        timedPost { uri =>
          val submissionId = SubmissionId(lei, period, seqNr)
          val uploadTimestamp = Instant.now.toEpochMilli

          val submissionPersistence =
            sharding.entityRefFor(
              SubmissionPersistence.typeKey,
              s"${SubmissionPersistence.name}-${submissionId.toString}")

          val fSubmission
            : Future[Option[Submission]] = submissionPersistence ? (ref =>
            GetSubmission(ref))

          val fCheckSubmission = for {
            s <- fSubmission.mapTo[Option[Submission]]
          } yield s

          onComplete(fCheckSubmission) {
            case Success(result) =>
              result match {
                case Some(submission) =>
                  if (submission.status == Created) {
                    uploadFile(uploadTimestamp, submission, uri)
                  } else {
                    submissionNotAvailable(submissionId, uri)
                  }
                case None =>
                  submissionNotAvailable(submissionId, uri)
              }
            case Failure(error) =>
              val errorResponse =
                ErrorResponse(500, error.getLocalizedMessage, uri.path)
              complete(
                ToResponseMarshallable(
                  StatusCodes.InternalServerError -> errorResponse))
          }
        }
    }

  private def submissionNotAvailable(submissionId: SubmissionId,
                                     uri: Uri): Route = {
    val errorResponse = ErrorResponse(
      400,
      s"Submission ${submissionId.toString} not available for upload",
      uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def uploadRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("institutions") {
            uploadHmdaFileRoute
          }
        }
      }
    }
  }

  private def uploadFile(uploadTimeStamp: Long,
                         submission: Submission,
                         uri: Uri): Route = {
    val splitLines =
      Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

    fileUpload("file") {
      case (_, byteSource) =>
        val fUploaded = byteSource
          .via(splitLines)
          .map(_.utf8String)
          //TODO: send messages to Kafka here
          //.mapAsync(parallelism = flowParallelism)(line =>)
          .runWith(Sink.ignore)

        onComplete(fUploaded) {
          case Success(_) =>
            complete(
              ToResponseMarshallable(
                StatusCodes.Accepted -> submission.copy(status = Uploaded)))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            val errorResponse =
              ErrorResponse(400, "Invalid file format", uri.path)
            complete(
              ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
        }

      case _ =>
        val errorResponse = ErrorResponse(400, "Invalid file format", uri.path)
        complete(
          ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
    }
  }

}
