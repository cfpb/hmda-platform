package hmda.api.http.filing.submissions

import java.time.Instant

import akka.actor
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives.{ complete, encodeResponse, fileUpload, handleRejections, onComplete, path, pathPrefix }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Framing, Sink }
import akka.util.{ ByteString, Timeout }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import com.typesafe.config.Config
import hmda.api.http.directives.HmdaTimeDirectives
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.ErrorResponseCodec._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.HmdaRawDataCommands.{ AddLine, HmdaRawDataCommand }
import hmda.messages.submission.HmdaRawDataEvents.HmdaRawDataEvent
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.model.filing.submission._
import hmda.persistence.submission.{ HmdaRawData, SubmissionManager, SubmissionPersistence }
import hmda.messages.submission.SubmissionManagerCommands.{ SubmissionManagerCommand, UpdateSubmissionStatus }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait UploadHttpApi extends HmdaTimeDirectives {

  implicit val system: actor.ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val log: LoggingAdapter
  val sharding: ClusterSharding
  implicit val timeout: Timeout
  val config: Config

  // institutions/<lei>/filings/<period>/submissions/<seqNr>
  def uploadHmdaFileRoute(oAuth2Authorization: OAuth2Authorization) =
    path(Segment / "filings" / Segment / "submissions" / IntNumber) { (lei, period, seqNr) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        timedPost { uri =>
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            val submissionId    = SubmissionId(lei, period, seqNr)
            val uploadTimestamp = Instant.now.toEpochMilli

            val submissionManager =
              sharding.entityRefFor(SubmissionManager.typeKey, s"${SubmissionManager.name}-${submissionId.toString}")

            val submissionPersistence =
              sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${submissionId.toString}")

            val hmdaRaw =
              sharding.entityRefFor(
                HmdaRawData.typeKey,
                s"${HmdaRawData.name}-${submissionId.toString}"
              )

            val fSubmission: Future[Option[Submission]] = submissionPersistence ? (ref => GetSubmission(ref))

            val fCheckSubmission = for {
              s <- fSubmission.mapTo[Option[Submission]]
            } yield s

            onComplete(fCheckSubmission) {
              case Success(result) =>
                result match {
                  case Some(submission) =>
                    if (submission.status == Created) {
                      uploadFile(hmdaRaw, submissionManager, uploadTimestamp, submission, uri)
                    } else {
                      submissionNotAvailable(submissionId, uri)
                    }
                  case None =>
                    submissionNotAvailable(submissionId, uri)
                }
              case Failure(error) =>
                failedResponse(StatusCodes.InternalServerError, uri, error)
            }
          }
        }
      }
    }

  def uploadRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("institutions") {
            uploadHmdaFileRoute(oAuth2Authorization)
          }
        }
      }
    }

  private def uploadFile(hmdaRaw: EntityRef[HmdaRawDataCommand],
                         submissionManager: EntityRef[SubmissionManagerCommand],
                         uploadTimeStamp: Long,
                         submission: Submission,
                         uri: Uri): Route = {
    val splitLines =
      Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

    fileUpload("file") {
      case (metadata, byteSource) if metadata.fileName.toLowerCase.endsWith(".txt") =>
        val modified = submission.copy(status = Uploading)
        submissionManager ! UpdateSubmissionStatus(modified)
        val fUploaded = byteSource
          .via(splitLines)
          .map(_.utf8String + "\n")
          .via(uploadFile(submission.id, hmdaRaw))
          .runWith(Sink.ignore)

        onComplete(fUploaded) {
          case Success(_) =>
            val fileName = metadata.fileName
            val modified =
              submission.copy(status = Uploaded, fileName = fileName)
            submissionManager ! UpdateSubmissionStatus(modified)
            complete(ToResponseMarshallable(StatusCodes.Accepted -> submission.copy(status = Uploaded)))
          case Failure(error) =>
            val failed = submission.copy(status = Failed)
            submissionManager ! UpdateSubmissionStatus(failed)
            log.error(error.getLocalizedMessage)
            val errorResponse =
              ErrorResponse(400, "Invalid file format", uri.path)
            complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
        }

      case _ =>
        val errorResponse = ErrorResponse(400, "Invalid file format", uri.path)
        complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
    }
  }

  private def uploadFile(submissionId: SubmissionId, hmdaRaw: EntityRef[HmdaRawDataCommand]) =
    Flow[String]
      .mapAsync(2)(line => persistLine(hmdaRaw, submissionId, line))

  private def persistLine(entityRef: EntityRef[HmdaRawDataCommand], submissionId: SubmissionId, data: String): Future[HmdaRawDataEvent] = {

    val response: Future[HmdaRawDataEvent] = entityRef ? (ref => AddLine(submissionId, Instant.now.toEpochMilli, data, Some(ref)))
    response
  }

}
