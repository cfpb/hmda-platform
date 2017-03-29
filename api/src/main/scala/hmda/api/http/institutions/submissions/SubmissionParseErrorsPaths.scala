package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.ParsingErrorSummary
import hmda.api.protocol.processing.ParserResultsProtocol
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.HmdaFileParser.{ GetStatePaginated, PaginatedFileParseState }
import hmda.persistence.processing.HmdaFileParser

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success, Try }

trait SubmissionParseErrorsPaths
    extends ParserResultsProtocol
    with RequestVerificationUtils
    with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<id>/parseErrors
  def submissionParseErrorsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "parseErrors") { (period, seqNr) =>
      timedGet { uri =>
        val supervisor = system.actorSelection("/user/supervisor")
        completeVerified(institutionId, period, seqNr, uri) {
          parameters('page.as[Int] ? 1) { (page: Int) =>
            val submissionID = SubmissionId(institutionId, period, seqNr)
            val fHmdaFileParser = (supervisor ? FindProcessingActor(HmdaFileParser.name, submissionID)).mapTo[ActorRef]
            val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionID.institutionId, submissionID.period)).mapTo[ActorRef]

            val fHmdaFileParseState = for {
              s <- fHmdaFileParser
              xs <- (s ? GetStatePaginated(page)).mapTo[PaginatedFileParseState]
              sa <- fSubmissionsActor
              sub <- (sa ? GetSubmissionById(submissionID)).mapTo[Submission]
            } yield (xs, sub.status)

            onComplete(fHmdaFileParseState) {
              case Success((state, status)) =>
                val summary = ParsingErrorSummary(
                  state.tsParsingErrors,
                  state.larParsingErrors,
                  uri.path.toString,
                  page,
                  state.totalErroredLines,
                  status
                )
                complete(ToResponseMarshallable(summary))
              case Failure(errors) =>
                completeWithInternalError(uri, errors)
            }
          }
        }
      }
    }
}
