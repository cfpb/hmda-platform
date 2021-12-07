package hmda.api.http.filing.submissions

import java.time.Instant
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.util.{ByteString, Timeout}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.directives.QuarterlyFilingAuthorization._
import hmda.api.http.model.ErrorResponse
import hmda.api.ws.WebSocketProgressTracker
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.HmdaRawDataCommands.{AddLines, HmdaRawDataCommand}
import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionManagerCommands.{SubmissionManagerCommand, UpdateSubmissionStatus}
import hmda.model.filing.submission._
import hmda.persistence.submission.HmdaRawData.selectHmdaRawData
import hmda.persistence.submission.SubmissionManager.selectSubmissionManager
import hmda.persistence.submission.SubmissionPersistence.selectSubmissionPersistence
import hmda.util.http.FilingResponseUtils._
import hmda.utils.YearUtils.Period
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object UploadHttpApi {
  def create(
              log: Logger,
              sharding: ClusterSharding
            )(implicit ec: ExecutionContext, t: Timeout, system: ActorSystem[_], mat: Materializer): OAuth2Authorization => Route =
    new UploadHttpApi(log, sharding)(ec, t, system, mat).uploadRoutes _
}

private class UploadHttpApi(log: Logger, sharding: ClusterSharding)(
  implicit ec: ExecutionContext,
  t: Timeout,
  system: ActorSystem[_],
  mat: Materializer
) {
  private val quarterlyFiler = quarterlyFilingAllowed(log, sharding) _

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

  // POST <lei>/filings/<year>/submissions/<seqNr>
  // POST <lei>/filings/<year>/quarter/<q>/submissions/<seqNr>
  private def uploadHmdaFileRoute(oauth2Authorization: OAuth2Authorization): Route =
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      pathPrefix(Segment / "filings") { lei =>
        (extractUri & post) { uri =>
          oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
            path(IntNumber / "submissions" / IntNumber) { (year, seqNr) =>
              checkAndUploadSubmission(lei, year, None, seqNr, uri)
            } ~ path(IntNumber / "quarter" / Quarter / "submissions" / IntNumber) { (year, quarter, seqNr) =>
              quarterlyFiler(lei, year) {
                checkAndUploadSubmission(lei, year, Option(quarter), seqNr, uri)
              }
            }
          }
        } ~
          // WEBSOCKET /institutions/<LEI>/filings/<year>/submissions/<seqNr>/progress
          // WEBSOCKET /institutions/<LEI>/filings/<year>/quarter/<q>/submissions/<seqNr>/progress
          (extractUri & get) { _ =>
            oauth2Authorization.authorizeTokenWithLei(lei)(_ =>
              path(IntNumber / "submissions" / IntNumber / "progress")((year, seqNr) =>
                handleWebSocketMessages(
                  WebSocketProgressTracker.websocketFlow(system, sharding, SubmissionId(lei, Period(year, None), seqNr))
                )
              ) ~ path(IntNumber / "quarter" / Quarter / "submissions" / IntNumber / "progress")((year, quarter, seqNr) =>
                handleWebSocketMessages(
                  WebSocketProgressTracker.websocketFlow(system, sharding, SubmissionId(lei, Period(year, Option(quarter)), seqNr))
                )
              )
            )
          }
      }
    }

  private def checkAndUploadSubmission(lei: String, year: Int, quarter: Option[String], seqNr: Int, uri: Uri): Route = {
    val submissionId                            = SubmissionId(lei, Period(year, quarter), seqNr)
    val uploadTimestamp                         = Instant.now.toEpochMilli
    val submissionManager                       = selectSubmissionManager(sharding, submissionId)
    val submissionPersistence                   = selectSubmissionPersistence(sharding, submissionId)
    val hmdaRaw                                 = selectHmdaRawData(sharding, submissionId)
    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (ref => GetSubmission(ref))

    onComplete(fSubmission) {
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

  private def uploadFile(
                          hmdaRaw: EntityRef[HmdaRawDataCommand],
                          submissionManager: EntityRef[SubmissionManagerCommand],
                          uploadTimeStamp: Long,
                          submission: Submission,
                          uri: Uri
                        ): Route = {
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

        fUploaded.onComplete {
          case Success(_)  => log.info(s"File upload completed for ${submission.id}")
          case Failure(ex) => log.error(s"File upload failed for ${submission.id} {}", ex)
        }

        onComplete(fUploaded) {
          case Success(_) =>
            val fileName = metadata.fileName
            val modified =
              submission.copy(status = Uploaded, fileName = fileName)
            submissionManager ! UpdateSubmissionStatus(modified)
            complete(StatusCodes.Accepted -> submission.copy(status = Uploaded))

          case Failure(error) =>
            val failed = submission.copy(status = Failed)
            submissionManager ! UpdateSubmissionStatus(failed)
            log.error(error.getLocalizedMessage)
            val errorResponse =
              ErrorResponse(400, "Invalid file format", uri.path)
            complete(StatusCodes.BadRequest -> errorResponse)
        }

      case _ =>
        val errorResponse = ErrorResponse(400, "Invalid file format", uri.path)
        complete((StatusCodes.BadRequest -> errorResponse))
    }
  }

  private def uploadFile(submissionId: SubmissionId, hmdaRaw: EntityRef[HmdaRawDataCommand]): Flow[String, LinesAdded, NotUsed] =
    Flow[String]
      .grouped(100)
      .mapAsync(1)(lines => persistLines(hmdaRaw, submissionId, lines))

  private def persistLines(entityRef: EntityRef[HmdaRawDataCommand], submissionId: SubmissionId, data: Seq[String]): Future[LinesAdded] = {
    val response: Future[LinesAdded] = entityRef ? (ref => AddLines(submissionId, Instant.now.toEpochMilli, data, Some(ref)))
    response
  }
}