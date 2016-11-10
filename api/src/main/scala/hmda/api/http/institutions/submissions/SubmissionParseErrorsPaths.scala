package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.fi.lar.LarProtocol
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission, SubmissionId }
import hmda.parser.fi.lar.ParsingErrorSummary
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.processing.HmdaFileParser.HmdaFileParseState
import hmda.persistence.processing.{ HmdaFileParser, HmdaFileValidator }
import hmda.persistence.processing.HmdaFileValidator.HmdaFileValidationState

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SubmissionParseErrorsPaths
    extends LarProtocol
    with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<id>/parseErrors
  def submissionParseErrorsPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "parseErrors") { (period, seqNr) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")
          val submissionID = SubmissionId(institutionId, period, seqNr)
          val fHmdaFileParser = (supervisor ? FindProcessingActor(HmdaFileParser.name, submissionID)).mapTo[ActorRef]

          val fHmdaFileParseState = for {
            s <- fHmdaFileParser
            xs <- (s ? GetState).mapTo[HmdaFileParseState]
          } yield xs

          onComplete(fHmdaFileParseState) {
            case Success(state) =>
              val summary = ParsingErrorSummary(state.tsParsingErrors, state.larParsingErrors)
              complete(ToResponseMarshallable(summary))
            case Failure(errors) =>
              completeWithInternalError(uri, errors)
          }
        }
      }
    }
}
