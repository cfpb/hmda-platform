package hmda.api.http.filing

import java.time.Instant

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.headers.RawHeader
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.messages.filing.FilingCommands.{ CreateFiling, GetFilingDetails }
import hmda.model.filing.{ Filing, FilingDetails, InProgress }
import hmda.persistence.filing.FilingPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.api.http.model.filing.submissions._
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionProcessingCommands.{ GetHmdaValidationErrorState, GetVerificationStatus }
import hmda.model.filing.submission.{ QualityMacroExists, VerificationStatus }
import hmda.model.institution.Institution
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.HmdaValidationError
import hmda.util.http.FilingResponseUtils._
import hmda.utils.YearUtils._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait FilingHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  def filingDetailsResponse(filingDetails: FilingDetails): Future[FilingDetailsResponse] = {
    val submissionResponsesF =
      filingDetails.submissions.map { s =>
        val entity =
          sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-${s.id}")
        val fStatus: Future[VerificationStatus]      = entity ? (reply => GetVerificationStatus(reply))
        val fEdits: Future[HmdaValidationErrorState] = entity ? (reply => GetHmdaValidationErrorState(s.id, reply))
        val fSubmissionAndVerified                   = fStatus.map(v => (s, v))
        val fQMExists                                = fEdits.map(r => QualityMacroExists(!r.quality.isEmpty, !r.`macro`.isEmpty))
        for {
          submissionAndVerified <- fSubmissionAndVerified
          (submision, verified) = submissionAndVerified
          qmExists              <- fQMExists
        } yield SubmissionResponse(submision, verified, qmExists)
      }
    val fSubmissionResponse = Future.sequence(submissionResponsesF)
    fSubmissionResponse.map(submissionResponses => FilingDetailsResponse(filingDetails.filing, submissionResponses))
  }

  //institutions/<lei>/filings/<period>
  def filingReadPath(oAuth2Authorization: OAuth2Authorization): Route =
    path("institutions" / Segment / "filings" / Segment) { (lei, period) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        val institutionPersistence = {
          if (period == "2018") {
            sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$lei")
          } else {
            sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$lei-$period")
          }
        }

        val filingPersistence =
          sharding.entityRefFor(FilingPersistence.typeKey, s"${FilingPersistence.name}-$lei-$period")

        val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
          ref => GetInstitution(ref)
        )

        val fEnriched: Future[Option[FilingDetailsResponse]] = {
          val fDetails: Future[Option[FilingDetails]] = filingPersistence ? (ref => GetFilingDetails(ref))
          fDetails.flatMap {
            case None =>
              Future.successful(None)

            case Some(filingDetails) =>
              filingDetailsResponse(filingDetails)
                .map(Option(_))
          }
        }

        val filingDetailsF: Future[(Option[Institution], Option[FilingDetailsResponse])] = for {
          i <- fInstitution
          d <- fEnriched
        } yield (i, d)

        timedPost { uri =>
          if (!isValidYear(period.toInt)) {
            complete(ErrorResponse(500, s"Invalid Year Provided: $period", uri.path))
          } else {
            respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
              onComplete(filingDetailsF) {
                case Success((None, _)) =>
                  entityNotPresentResponse("institution", lei, uri)
                case Success((Some(_), Some(_))) =>
                  val errorResponse =
                    ErrorResponse(400, s"Filing $lei-$period already exists", uri.path)
                  complete(StatusCodes.BadRequest -> errorResponse)

                case Success((Some(_), None)) =>
                  val now = Instant.now().toEpochMilli
                  val filing = Filing(
                    period,
                    lei,
                    InProgress,
                    true,
                    now,
                    0L
                  )
                  val fFiling: Future[FilingCreated] = filingPersistence ? (ref => CreateFiling(filing, ref))
                  onComplete(fFiling) {
                    case Success(created) =>
                      val filingDetails =
                        FilingDetailsResponse(created.filing, Nil)
                      complete(StatusCodes.Created -> filingDetails)
                    case Failure(error) =>
                      failedResponse(StatusCodes.InternalServerError, uri, error)
                  }
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)
              }
            }
          }
        } ~
          timedGet { uri =>
            if (!isValidYear(period.toInt)) {
              complete(ErrorResponse(500, s"Invalid Year Provided: $period", uri.path))
            } else {
              respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
                onComplete(filingDetailsF) {
                  case Success((Some(_), Some(filingDetailsResponse))) =>
                    complete(filingDetailsResponse)
                  case Success((None, _)) =>
                    entityNotPresentResponse("institution", lei, uri)
                  case Success((Some(i), None)) =>
                    val errorResponse = ErrorResponse(404, s"Filing for institution: ${i.LEI} and period: $period does not exist", uri.path)
                    complete(
                      ToResponseMarshallable(StatusCodes.NotFound -> errorResponse)
                    )
                  case Failure(error) =>
                    failedResponse(StatusCodes.InternalServerError, uri, error)
                }
              }

            }
          }
      }
    }

  def filingRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filingReadPath(oAuth2Authorization)
        }
      }
    }
}
