package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives.{ encodeResponse, handleRejections }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import hmda.api.http.directives.{ HmdaTimeDirectives, QuarterlyFilingAuthorization }
import hmda.api.http.model.filing.submissions.{ EditsSign, SignedResponse }
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.SignSubmission
import hmda.messages.submission.SubmissionProcessingEvents.{ SubmissionNotReadyToBeSigned, SubmissionSigned, SubmissionSignedEvent }
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.PathMatchers._
import hmda.persistence.submission.HmdaValidationError.selectHmdaValidationError
import hmda.persistence.submission.SubmissionPersistence.selectSubmissionPersistence
import hmda.utils.YearUtils.Period
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SignHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  // GET & POST institutions/<lei>/filings/<year>/submissions/<submissionId>/sign
  // GET & POST institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/sign
  def signPath(oAuth2Authorization: OAuth2Authorization): Route =
    pathPrefix("institutions" / Segment / "filings" / IntNumber) { (lei, year) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { token =>
        pathPrefix("submissions" / IntNumber / "sign") { seqNr =>
          timedGet { uri =>
            getSubmissionForSigning(lei, year, None, seqNr, token.email, uri)
          } ~ timedPost { uri =>
            respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
              entity(as[EditsSign]) { editsSign =>
                signSubmission(lei, year, None, seqNr, token.email, editsSign.signed, uri)
              }
            }
          }
        } ~ pathPrefix("quarter" / Quarter / "submissions" / IntNumber / "sign") { (quarter, seqNr) =>
            timedGet { uri =>
              quarterlyFilingAllowed(lei, year) {
                getSubmissionForSigning(lei, year, Option(quarter), seqNr, token.email, uri)
              }
            } ~ timedPost { uri =>
              respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
                entity(as[EditsSign]) { editsSign =>
                  quarterlyFilingAllowed(lei, year) {
                    signSubmission(lei, year, Option(quarter), seqNr, token.email, editsSign.signed, uri)
                  }
                }
              }
            }
        }
      }
    }

  private def getSubmissionForSigning(lei: String, year: Int, quarter: Option[String], seqNr: Int, email: String, uri: Uri): Route = {
    val submissionId                            = SubmissionId(lei, Period(year, quarter), seqNr)
    val submissionPersistence                   = selectSubmissionPersistence(sharding, submissionId)
    val fSubmission: Future[Option[Submission]] = submissionPersistence ? GetSubmission
    onComplete(fSubmission) {
      case Failure(e) =>
        failedResponse(StatusCodes.InternalServerError, uri, e)

      case Success(None) =>
        submissionNotAvailable(submissionId, uri)

      case Success(Some(submission)) =>
        val signed = SignedResponse(email, submission.end, submission.receipt, submission.status)
        complete(signed)
    }
  }

  private def signSubmission(
    lei: String,
    year: Int,
    quarter: Option[String],
    seqNr: Int,
    email: String,
    signed: Boolean,
    uri: Uri
  ): Route = {
    val submissionId = SubmissionId(lei, Period(year, quarter), seqNr)
    if (!signed) badRequest(submissionId, uri, "Illegal argument: signed = false")
    else {
      val hmdaValidationError                    = selectHmdaValidationError(sharding, submissionId)
      val fSigned: Future[SubmissionSignedEvent] = hmdaValidationError ? (ref => SignSubmission(submissionId, ref, email))
      onComplete(fSigned) {
        case Failure(e) =>
          failedResponse(StatusCodes.InternalServerError, uri, e)

        case Success(submissionSignedEvent) =>
          submissionSignedEvent match {
            case signed @ SubmissionSigned(_, _, status) =>
              val signedResponse = SignedResponse(email, signed.timestamp, signed.receipt, status)
              complete(ToResponseMarshallable(signedResponse))

            case SubmissionNotReadyToBeSigned(id) =>
              badRequest(id, uri, s"Submission $id is not ready to be signed")
          }
      }
    }
  }

  def signRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          signPath(oAuth2Authorization)
        }
      }
    }
}
