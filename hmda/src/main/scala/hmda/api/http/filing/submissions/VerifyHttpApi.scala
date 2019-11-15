package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.{ HmdaTimeDirectives, QuarterlyFilingAuthorization }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import hmda.api.http.model.filing.submissions.{ EditsVerification, EditsVerificationResponse }
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.persistence.submission.{ HmdaValidationError, SubmissionPersistence }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.messages.submission.SubmissionProcessingCommands.{ VerifyMacro, VerifyQuality }
import hmda.util.http.FilingResponseUtils._
import hmda.messages.submission.SubmissionProcessingEvents.{
  MacroVerified,
  NotReadyToBeVerified,
  QualityVerified,
  SubmissionProcessingEvent
}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.auth.OAuth2Authorization
import hmda.api.http.PathMatchers._
import hmda.utils.YearUtils

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex
import scala.util.{ Failure, Success }

trait VerifyHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  override val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  private val editTypeRegex = new Regex("quality|macro")

  // POST institutions/<lei>/filings/<year>/submissions/<submissionId>/edits/<quality|macro>
  // POST institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/edits/<quality|macro>
  def verifyPath(oAuth2Authorization: OAuth2Authorization): Route =
    timedPost { uri =>
      respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
        entity(as[EditsVerification]) { editsVerification =>
          pathPrefix("institutions" / Segment / "filings" / IntNumber) { (lei, year) =>
            oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
              path("submissions" / IntNumber / "edits" / editTypeRegex) { (seqNr, editType) =>
                verify(lei, year, None, seqNr, editType, editsVerification.verified, uri)
              } ~ path("quarter" / Quarter / "submissions" / IntNumber / "edits" / editTypeRegex) { (quarter, seqNr, editType) =>
                pathEndOrSingleSlash {
                  quarterlyFilingAllowed(lei, year) {
                    verify(lei, year, Option(quarter), seqNr, editType, editsVerification.verified, uri)
                  }
                }
              }
            }
          }
        }
      }
    }

  private def verify(lei: String, year: Int, quarter: Option[String], seqNr: Int, editType: String, verified: Boolean, uri: Uri): Route = {
    val period                                  = YearUtils.period(year, quarter)
    val submissionId                            = SubmissionId(lei, period, seqNr)
    val submissionPersistence                   = SubmissionPersistence.selectSubmissionPersistence(sharding, submissionId)
    val fSubmission: Future[Option[Submission]] = submissionPersistence ? GetSubmission

    val validationPersistence = HmdaValidationError.selectHmdaValidationError(sharding, submissionId)
    val fVerified: Future[SubmissionProcessingEvent] = validationPersistence ? (
      ref =>
        editType match {
          case "quality" => VerifyQuality(submissionId, verified, ref)
          case "macro"   => VerifyMacro(submissionId, verified, ref)
        }
      )

    val fVerification = for {
      submission <- fSubmission
      verified   <- fVerified
    } yield (submission, verified)

    onComplete(fVerification) {
      case Success(result) =>
        result match {
          case (None, _) =>
            submissionNotAvailable(submissionId, uri)

          case (Some(_), verifiedStatus) =>
            verifiedStatus match {
              case NotReadyToBeVerified(_) =>
                badRequest(submissionId, uri, s"Submission $submissionId is not ready to be verified")

              case QualityVerified(_, verified, status) =>
                val response =
                  EditsVerificationResponse(verified, status)
                complete(ToResponseMarshallable(response))

              case MacroVerified(_, verified, status) =>
                val response =
                  EditsVerificationResponse(verified, status)
                complete(ToResponseMarshallable(response))

              case _ =>
                badRequest(submissionId, uri, "Incorrect response event")
            }
        }
      case Failure(e) =>
        failedResponse(StatusCodes.InternalServerError, uri, e)
    }
  }

  def verifyRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          verifyPath(oAuth2Authorization)
        }
      }
    }

}
