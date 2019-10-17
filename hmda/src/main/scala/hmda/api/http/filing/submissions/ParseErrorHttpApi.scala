package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.GetParsingErrors
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.model.processing.state.HmdaParserErrorState
import hmda.persistence.submission.{HmdaParserError, SubmissionPersistence}
import hmda.util.http.FilingResponseUtils._
import hmda.model.filing.ParserValidValuesLookup._
import hmda.api.http.model.filing.submissions._
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait ParseErrorHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  val log: LoggingAdapter
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>/filings/<period>/submissions/<submissionId>/parseErrors
  def parseErrorPath(oAuth2Authorization: OAuth2Authorization): Route =
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "parseErrors") {
      (lei, period, seqNr) =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          timedGet { uri =>
            parameters('page.as[Int] ? 1) { page =>
              val submissionId = SubmissionId(lei, period, seqNr)

              val submissionPersistence =
                sharding.entityRefFor(
                  SubmissionPersistence.typeKey,
                  s"${SubmissionPersistence.name}-${submissionId.toString}")

              val fSubmission
              : Future[Option[Submission]] = submissionPersistence ? (ref =>
                GetSubmission(ref))

              val fCheckSubmission = for {
                s <- fSubmission.mapTo[Option[Submission]]
              } yield s

              onComplete(fCheckSubmission) {
                case Success(check) =>
                  check match {
                    case Some(submission) =>
                      val hmdaParserError =
                        sharding.entityRefFor(
                          HmdaParserError.typeKey,
                          s"${HmdaParserError.name}-${submissionId.toString}")
                      val fErrors
                        : Future[HmdaParserErrorState] = hmdaParserError ? (
                          ref => GetParsingErrors(page, ref))
                      onComplete(fErrors) {
                        case Success(state) =>
                          val parsingErrorSummary = ParsingErrorSummary(
                            state.transmittalSheetErrors.map(
                              parserErrorSummaryConvertor(_)),
                            state.larErrors.map(parserErrorSummaryConvertor(_)),
                            uri.path.toString,
                            page,
                            state.totalErrors,
                            submission.status
                          )
                          complete(parsingErrorSummary)
                        case Failure(error) =>
                          failedResponse(StatusCodes.InternalServerError,
                            uri,
                            error)
                      }

                    case None =>
                      entityNotPresentResponse("submission",
                        submissionId.toString,
                        uri)
                  }
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)
              }
            }
          }
        }
    }

  def parserErrorRoute(oAuth2Authorization: OAuth2Authorization): Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          parseErrorPath(oAuth2Authorization)
        }
      }
    }
  }

  def parserErrorSummaryConvertor(
      hmdaRowParsedError: HmdaRowParsedError): HmdaRowParsedErrorSummary = {
    HmdaRowParsedErrorSummary(
      hmdaRowParsedError.rowNumber,
      hmdaRowParsedError.estimatedULI,
      hmdaRowParsedError.errorMessages.map(
        errorMessage =>
          FieldParserErrorSummary(
            errorMessage.fieldName,
            errorMessage.inputValue,
            lookupParserValidValues(errorMessage.fieldName)
        ))
    )
  }

}
