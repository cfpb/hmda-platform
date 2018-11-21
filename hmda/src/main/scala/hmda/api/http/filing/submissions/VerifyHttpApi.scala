package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.api.http.model.filing.submissions.{
  EditsVerification,
  EditsVerificationResponse
}
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.persistence.submission.{HmdaValidationError, SubmissionPersistence}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.messages.submission.SubmissionProcessingCommands.{
  VerifyMacro,
  VerifyQuality
}
import hmda.util.http.FilingResponseUtils._
import hmda.messages.submission.SubmissionProcessingEvents.{
  MacroVerified,
  NotReadyToBeVerified,
  QualityVerified,
  SubmissionProcessingEvent
}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import hmda.auth.OAuth2Authorization
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex
import scala.util.{Failure, Success}

trait VerifyHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  override val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>/filings/<period>/submissions/<submissionId>/edits/<quality|macro>
  private val editTypeRegex = new Regex("quality|macro")
  def verifyPath(oAuth2Authorization: OAuth2Authorization): Route =
    oAuth2Authorization.authorizeToken { _ =>
      path(
        "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "edits" / editTypeRegex) {
        (lei, period, seqNr, editType) =>
          timedPost { uri =>
            entity(as[EditsVerification]) { editsVerification =>
              val submissionId = SubmissionId(lei, period, seqNr)

              val submissionPersistence =
                sharding.entityRefFor(
                  SubmissionPersistence.typeKey,
                  s"${SubmissionPersistence.name}-${submissionId.toString}")

              val fSubmission
                : Future[Option[Submission]] = submissionPersistence ? (ref =>
                GetSubmission(ref))

              val validationPersistence = sharding.entityRefFor(
                HmdaValidationError.typeKey,
                s"${HmdaValidationError.name}-$submissionId")

              val fVerified
                : Future[SubmissionProcessingEvent] = validationPersistence ? {
                ref =>
                  editType match {
                    case "quality" =>
                      VerifyQuality(submissionId,
                                    editsVerification.verified,
                                    ref)
                    case "macro" =>
                      VerifyMacro(submissionId, editsVerification.verified, ref)
                  }
              }

              val fVerification = for {
                submission <- fSubmission
                verified <- fVerified
              } yield (submission, verified)

              onComplete(fVerification) {
                case Success(result) =>
                  result match {
                    case (None, _) =>
                      submissionNotAvailable(submissionId, uri)
                    case (Some(s), verifiedStatus) =>
                      verifiedStatus match {
                        case NotReadyToBeVerified(_) =>
                          badRequest(
                            submissionId,
                            uri,
                            s"Submission $submissionId is not ready to be verified")

                        case QualityVerified(_, verified, status) =>
                          val response =
                            EditsVerificationResponse(verified, status)
                          complete(ToResponseMarshallable(response))

                        case MacroVerified(_, verified) =>
                          val response =
                            EditsVerificationResponse(verified, s.status)
                          complete(ToResponseMarshallable(response))

                        case _ =>
                          badRequest(submissionId,
                                     uri,
                                     "Incorrect response event")
                      }
                  }
                case Failure(e) =>
                  failedResponse(StatusCodes.InternalServerError, uri, e)
              }
            }
          }
      }
    }

  def verifyRoutes(oAuth2Authorization: OAuth2Authorization): Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          verifyPath(oAuth2Authorization)
        }
      }
    }
  }

}
