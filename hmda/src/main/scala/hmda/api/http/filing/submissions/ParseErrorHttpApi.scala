package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.{ HmdaTimeDirectives, QuarterlyFilingAuthorization }
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.GetParsingErrors
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.submission.{ HmdaParserError, SubmissionPersistence }
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.model.filing.submissions._
import hmda.api.http.PathMatchers._
import hmda.utils.YearUtils.Period
import hmda.api.http.utils.ParserErrorUtils._
import HmdaTimeDirectives._
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait ParseErrorHttpApi extends QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val log: Logger
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  // GET institutions/<lei>/filings/<year>/submissions/<submissionId>/parseErrors
  // GET institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/parseErrors
  def parseErrorPath(oauth2Authorization: OAuth2Authorization): Route = (extractUri & get) { uri =>
    parameters('page.as[Int] ? 1) { page =>
      pathPrefix("institutions" / Segment / "filings" / IntNumber) { (lei, year) =>
        oauth2Authorization.authorizeTokenWithLei(lei) { _ =>
          path("submissions" / IntNumber / "parseErrors") { seqNr =>
            checkSubmission(lei, year, None, seqNr, page, uri)
          } ~ path("quarter" / Quarter / "submissions" / IntNumber / "parseErrors") { (quarter, seqNr) =>
            pathEndOrSingleSlash {
              quarterlyFilingAllowed(lei, year) {
                checkSubmission(lei, year, Option(quarter), seqNr, page, uri)
              }
            }
          }
        }
      }
    }
  }

  private def checkSubmission(lei: String, year: Int, quarter: Option[String], seqNr: Int, page: Int, uri: Uri) = {
    val submissionId                            = SubmissionId(lei, Period(year, quarter), seqNr)
    val submissionPersistence                   = SubmissionPersistence.selectSubmissionPersistence(sharding, submissionId)
    val fSubmission: Future[Option[Submission]] = submissionPersistence ? GetSubmission
    val fCheckSubmission = for {
      s <- fSubmission.mapTo[Option[Submission]]
    } yield s

    onComplete(fCheckSubmission) {
      case Success(check) =>
        check match {
          case Some(submission) =>
            val hmdaParserError =
              sharding.entityRefFor(HmdaParserError.typeKey, s"${HmdaParserError.name}-${submissionId.toString}")
            val fErrors: Future[HmdaParserErrorState] = hmdaParserError ? (ref => GetParsingErrors(page, ref))
            onComplete(fErrors) {
              case Success(state) =>
                val parsingErrorSummary = ParsingErrorSummary(
                  state.transmittalSheetErrors.map(parserErrorSummaryConvertor),
                  state.larErrors.map(parserErrorSummaryConvertor),
                  uri.path.toString,
                  page,
                  state.totalErrors,
                  submission.status
                )
                complete(parsingErrorSummary)
              case Failure(error) =>
                failedResponse(StatusCodes.InternalServerError, uri, error)
            }

          case None =>
            entityNotPresentResponse("submission", submissionId.toString, uri)
        }
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
  }

  def parserErrorRoute(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          timed(parseErrorPath(oAuth2Authorization))
        }
      }
    }
}