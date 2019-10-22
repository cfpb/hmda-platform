package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.{ PathMatcher1, Route }
import akka.util.{ ByteString, Timeout }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.{ ByteString, Timeout }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.api.http.model.filing.submissions.SubmissionResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands._
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.SubmissionCreated
import hmda.messages.submission.SubmissionProcessingCommands.GetVerificationStatus
import hmda.model.filing.{ Filing, FilingDetails }
import hmda.model.filing.submission._
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.HmdaProcessingUtils._
import hmda.persistence.submission._
import hmda.util.http.FilingResponseUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.api.http.PathMatchers._
import hmda.persistence.filing.FilingPersistence.selectFiling
import hmda.persistence.institution.InstitutionPersistence.selectInstitution
import hmda.utils.YearUtils
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>/filings/<period>/submissions
  def submissionCreatePath(oauth2Authorization: OAuth2Authorization): Route =
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      timedPost { uri =>
        path("institutions" / Segment / "filings" / Year / "submissions") { (lei, year) =>
          oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
            createSubmissionIfValid(lei, year, None, uri)
          }
        } ~ path("institutions" / Segment / "filings" / Year / "quarter" / Quarter / "submissions") { (lei, year, quarter) =>
          oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
            createSubmissionIfValid(lei, year, Option(quarter), uri)
          }
        }
      }
    }

  def createSubmissionIfValid(lei: String, year: Int, quarter: Option[String], uri: Uri): Route = {
    val period = YearUtils.period(year, quarter)
    onComplete(obtainLatestSubmission(lei, year, quarter)) {
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

  def obtainLatestSubmission(
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

  def submissionSummary(oAuth2Authorization: OAuth2Authorization): Route =
    path("institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "summary") { (lei, period, seqNr) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        timedGet { uri =>
          val submissionId = SubmissionId(lei, period, seqNr)

          val filingPersistence =
            sharding.entityRefFor(FilingPersistence.typeKey, s"${FilingPersistence.name}-$lei-$period")

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
                case SubmissionSummary(None, _) =>
                  val errorResponse = ErrorResponse(404, s"Submission ${submissionId.toString} not available", uri.path)
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
                case SubmissionSummary(_, None) =>
                  val errorResponse =
                    ErrorResponse(404, s"Transmittal Sheet not found", uri.path)
                  complete(StatusCodes.NotFound -> errorResponse)
                case _ =>
                  complete(ToResponseMarshallable(check))
              }
            case Failure(error) =>
              failedResponse(StatusCodes.InternalServerError, uri, error)
          }
        }
      }
    }

  //institutions/<lei>/filings/<period>/submissions/latest
  def submissionLatestPath(oAuth2Authorization: OAuth2Authorization): Route =
    path("institutions" / Segment / "filings" / Segment / "submissions" / "latest") { (lei, period) =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
          timedGet { uri =>
            val filingPersistence =
              sharding.entityRefFor(FilingPersistence.typeKey, s"${FilingPersistence.name}-$lei-$period")

            val fLatest: Future[Option[Submission]] = filingPersistence ? (ref => GetLatestSubmission(ref))

            val fResponse = fLatest.flatMap {
              case Some(s) =>
                val entity =
                  sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-${s.id}")
                val fStatus: Future[VerificationStatus] = entity ? (reply => GetVerificationStatus(reply))
                fStatus.map(v => Option(SubmissionResponse(s, v)))

              case None =>
                Future.successful(None)
            }

            onComplete(fResponse) {
              case Success(maybeLatest) =>
                maybeLatest match {
                  case Some(latest) =>
                    complete(latest)

                  case None =>
                    complete(StatusCodes.NotFound)
                }
              case Failure(error) =>
                failedResponse(StatusCodes.InternalServerError, uri, error)
            }
          }
        }
      }
    }

  def submissionRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionCreatePath(oAuth2Authorization) ~ submissionLatestPath(oAuth2Authorization) ~ submissionSummary(oAuth2Authorization)
        }
      }
    }

  private def createSubmission(uri: Uri, submissionId: SubmissionId): Route = {
    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${submissionId.toString}")

    val createdF: Future[SubmissionCreated] = submissionPersistence ? (ref => CreateSubmission(submissionId, ref))

    onComplete(createdF) {
      case Success(created) =>
        complete(StatusCodes.Created -> created.submission)
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

}
