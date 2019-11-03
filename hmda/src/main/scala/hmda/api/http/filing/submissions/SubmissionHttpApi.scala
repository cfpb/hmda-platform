package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, Uri }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.{ ByteString, Timeout }
import hmda.api.http.directives.{ HmdaTimeDirectives, QuarterlyFilingAuthorization }
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.messages.filing.FilingCommands.{ GetFiling, GetLatestSubmission, GetSubmissionSummary }
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.SubmissionCreated
import hmda.model.filing.Filing
import hmda.model.filing.submission.{ QualityMacroExists, Submission, SubmissionId, SubmissionSummary, VerificationStatus }
import hmda.model.institution.Institution
import hmda.persistence.submission.{ HmdaValidationError, SubmissionPersistence }
import hmda.auth.OAuth2Authorization
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.submission.HmdaProcessingUtils._
import hmda.api.http.model.ErrorResponse
import hmda.util.http.FilingResponseUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.api.http.PathMatchers._
import hmda.api.http.model.filing.submissions.SubmissionResponse
import hmda.messages.submission.SubmissionProcessingCommands.{ GetHmdaValidationErrorState, GetVerificationStatus }
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.persistence.filing.FilingPersistence.selectFiling
import hmda.persistence.institution.InstitutionPersistence.selectInstitution
import hmda.utils.YearUtils

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  def submissionCreatePath(oauth2Authorization: OAuth2Authorization): Route =
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      timedPost { uri =>
        path("institutions" / Segment / "filings" / Year / "submissions") { (lei, year) =>
          oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
            createSubmissionIfValid(lei, year, None, uri)
          }
        } ~ path("institutions" / Segment / "filings" / Year / "quarter" / Quarter / "submissions") { (lei, year, quarter) =>
          oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
            pathEndOrSingleSlash {
              quarterlyFilingAllowed(lei, year) {
                createSubmissionIfValid(lei, year, Option(quarter), uri)
              }
            }
          }
        }
      }
    }

  private def createSubmissionIfValid(lei: String, year: Int, quarter: Option[String], uri: Uri): Route = {
    val period = YearUtils.period(year, quarter)
    onComplete(obtainLatestSubmissionAndFilingAndInstitution(lei, year, quarter)) {
      case Success(check) =>
        check match {
          case (None, _, _) =>
            entityNotPresentResponse("institution", lei, uri)
          case (_, None, _) =>
            entityNotPresentResponse("filing", period, uri)
          case (_, _, maybeLatest) =>
            maybeLatest match {
              case None =>
                val submissionId = SubmissionId(lei, period, 1)
                createSubmission(uri, submissionId)

              case Some(submission) =>
                val submissionId = SubmissionId(lei, period, submission.id.sequenceNumber + 1)
                createSubmission(uri, submissionId)
            }
        }

      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  private def createSubmission(uri: Uri, submissionId: SubmissionId): Route = {
    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${submissionId.toString}")

    val createdF: Future[SubmissionCreated] = submissionPersistence ? (ref => CreateSubmission(submissionId, ref))

    onComplete(createdF) {
      case Success(created) =>
        complete(StatusCodes.Created, created.submission)
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  private def obtainLatestSubmissionAndFilingAndInstitution(
    lei: String,
    period: Int,
    quarter: Option[String]
  ): Future[(Option[Institution], Option[Filing], Option[Submission])] = {
    val ins = selectInstitution(sharding, lei, period)
    val fil = selectFiling(sharding, lei, period, quarter)

    val fInstitution: Future[Option[Institution]]     = ins ? (ref => GetInstitution(ref))
    val fFiling: Future[Option[Filing]]               = fil ? (ref => GetFiling(ref))
    val fLatestSubmission: Future[Option[Submission]] = fil ? (ref => GetLatestSubmission(ref))

    for {
      i <- fInstitution
      f <- fFiling
      l <- fLatestSubmission
    } yield (i, f, l)
  }

  def submissionSummaryPath(oAuth2Authorization: OAuth2Authorization): Route =
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      timedGet { uri =>
        path("institutions" / Segment / "filings" / Year / "submissions" / IntNumber / "summary") { (lei, year, seqNr) =>
          oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
            getSubmissionSummary(lei, year, None, seqNr, uri)
          }
        } ~ path("institutions" / Segment / "filings" / Year / "quarter" / Quarter / "submissions" / IntNumber / "summary") {
          (lei, year, quarter, seqNr) =>
            oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
              pathEndOrSingleSlash {
                quarterlyFilingAllowed(lei, year) { _ =>
                  getSubmissionSummary(lei, year, Option(quarter), seqNr, uri)
                }
              }
            }
        }
      }
    }

  private def getSubmissionSummary(lei: String, year: Int, quarter: Option[String], seqNr: Int, uri: Uri): Route = {
    val period                               = YearUtils.period(year, quarter)
    val submissionId                         = SubmissionId(lei, period, seqNr)
    val filingPersistence                    = selectFiling(sharding, lei, year, quarter)
    val fSummary: Future[Option[Submission]] = filingPersistence ? (ref => GetSubmissionSummary(submissionId, ref))
    val fTs: Future[Option[TransmittalSheet]] =
      readRawData(submissionId)
        .map(line => line.data)
        .map(ByteString(_))
        .take(1)
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .map(s => TsCsvParser(s))
        .map { s =>
          s.getOrElse(TransmittalSheet())
        }
        .runWith(Sink.seq)
        .map(xs => xs.headOption)

    val fCheck = for {
      t <- fTs
      s <- fSummary
    } yield SubmissionSummary(s, t)

    onComplete(fCheck) {
      case Success(check) =>
        check match {
          case (SubmissionSummary(None, _)) =>
            val errorResponse = ErrorResponse(404, s"Submission ${submissionId.toString} not available", uri.path)
            complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
          case (SubmissionSummary(_, None)) =>
            val errorResponse =
              ErrorResponse(404, s"Transmittal Sheet not found", uri.path)
            complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
          case _ =>
            complete(ToResponseMarshallable(check))
        }
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  def latestSubmissionPath(oAuth2Authorization: OAuth2Authorization): Route =
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      timedGet { uri =>
        path("institutions" / Segment / "filings" / Year / "submissions" / "latest") { (lei, year) =>
          oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
            getLatestSubmission(lei, year, None, uri)
          }
        } ~ path("institutions" / Segment / "filings" / Year / "quarter" / Quarter / "submissions" / "latest") { (lei, year, quarter) =>
          oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
            pathEndOrSingleSlash {
              quarterlyFilingAllowed(lei, year) { _ =>
                getLatestSubmission(lei, year, Option(quarter), uri)
              }
            }
          }
        }
      }
    }

  private def getLatestSubmission(lei: String, period: Int, quarter: Option[String], uri: Uri): Route = {
    val fil                                 = selectFiling(sharding, lei, period, quarter)
    val fLatest: Future[Option[Submission]] = fil ? (ref => GetLatestSubmission(ref))
    val fResponse = fLatest.flatMap {
      case Some(s) =>
        val entity =
          sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-${s.id}")
        val fStatus: Future[VerificationStatus]      = entity ? (reply => GetVerificationStatus(reply))
        val fEdits: Future[HmdaValidationErrorState] = entity ? (reply => GetHmdaValidationErrorState(s.id, reply))

        val fSubmissionAndVerified = fStatus.map(v => (s, v))

        val fQMExists = fEdits.map(r => QualityMacroExists(!r.quality.isEmpty, !r.`macro`.isEmpty))

        for {
          submissionAndVerified <- fSubmissionAndVerified
          (submision, verified) = submissionAndVerified
          qmExists              <- fQMExists
        } yield Option(SubmissionResponse(submision, verified, qmExists))
      case None =>
        Future.successful(None)
    }
    onComplete(fResponse) {
      case Success(maybeLatest) =>
        maybeLatest match {
          case Some(latest) =>
            complete(ToResponseMarshallable(latest))
          case None => complete(HttpResponse(StatusCodes.NotFound))
        }
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  def submissionRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionCreatePath(oAuth2Authorization) ~ latestSubmissionPath(oAuth2Authorization) ~ submissionSummaryPath(oAuth2Authorization)
        }
      }
    }
}
