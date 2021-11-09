package hmda.api.http.filing.submissions

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.directives.QuarterlyFilingAuthorization._
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.api.http.utils.ParserErrorUtils._
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.GetParsingErrors
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.submission.{ HmdaParserError, SubmissionPersistence }
import hmda.util.http.FilingResponseUtils._
import hmda.utils.YearUtils.Period
import org.slf4j.Logger
import hmda.auth._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ParseErrorHttpApi {
  def create(log: Logger, sharding: ClusterSharding)(implicit ec: ExecutionContext, system: ActorSystem[_], t: Timeout): OAuth2Authorization => Route =
    new ParseErrorHttpApi(log, sharding)(ec, system, t).parserErrorRoute _
}

private class ParseErrorHttpApi(log: Logger, sharding: ClusterSharding)(implicit ec: ExecutionContext, system: ActorSystem[_], t: Timeout) {
  val quarterlyFiler = quarterlyFilingAllowed(log, sharding) _
  val config           = system.settings.config
  val currentNamespace = config.getString("hmda.currentNamespace")

  // GET institutions/<lei>/filings/<year>/submissions/<submissionId>/parseErrors
  // GET institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/parseErrors
  def parseErrorPath(oAuth2Authorization: OAuth2Authorization): Route = (extractUri & get) { uri =>
    parameters('page.as[Int] ? 1) { page =>
      path("institutions" / Segment / "filings" / IntNumber / "submissions" / IntNumber / "parseErrors") { (lei, year, seqNr) =>
        oAuth2Authorization.authorizeTokenWithRule(LEISpecificOrAdmin, lei) { _ =>
          oAuth2Authorization.authorizeTokenWithRule(BetaOnlyUser, currentNamespace) { token =>
            checkSubmission(lei, year, None, seqNr, page, uri)
          }
        }
      } ~ path("institutions" / Segment / "filings" / IntNumber / "quarter" / Quarter / "submissions" / IntNumber / "parseErrors") { (lei, year, quarter, seqNr) =>
            oAuth2Authorization.authorizeTokenWithRule(LEISpecificOrAdmin, lei) { _ =>
              oAuth2Authorization.authorizeTokenWithRule(BetaOnlyUser, currentNamespace) { token =>
                pathEndOrSingleSlash {
                  quarterlyFiler(lei, year) {
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
          parseErrorPath(oAuth2Authorization)
        }
      }
    }
}