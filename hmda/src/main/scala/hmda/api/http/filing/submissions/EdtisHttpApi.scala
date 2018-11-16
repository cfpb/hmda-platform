package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{encodeResponse, handleRejections}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{
  cors,
  corsRejectionHandler
}
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.messages.submission.SubmissionProcessingCommands.GetHmdaValidationErrorState
import hmda.model.filing.submission.{SubmissionId, SubmissionStatus}
import hmda.model.processing.state.{EditSummary, HmdaValidationErrorState}
import hmda.persistence.submission.HmdaValidationError
import hmda.util.http.FilingResponseUtils._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait EdtisHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<institutionId>/filings/<period>/submissions/<submissionId>/edits
  val editsSummaryPath: Route =
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "edits") {
      (lei, period, seqNr) =>
        timedGet { uri =>
          val submissionId = SubmissionId(lei, period, seqNr)
          val hmdaValidationError = sharding
            .entityRefFor(HmdaValidationError.typeKey,
                          s"${HmdaValidationError.name}-$submissionId")

          val fEdits: Future[HmdaValidationErrorState] = hmdaValidationError ? (
              ref => GetHmdaValidationErrorState(submissionId, ref))

          onComplete(fEdits) {
            case Success(edits) =>
              val syntactical = SyntacticalEditSummaryResponse(
                edits.syntactical.map(toEditSummaryResponse).toSeq)
              val validity = ValidityEditSummaryResponse(
                edits.validity.map(toEditSummaryResponse).toSeq)
              val quality = QualityEditSummaryResponse(
                edits.quality.map(toEditSummaryResponse).toSeq,
                edits.qualityVerified)
              val `macro` = MacroEditSummaryResponse(
                edits.`macro`.map(toEditSummaryResponse).toSeq,
                edits.macroVerified)
              val editsSummaryResponse =
                EditsSummaryResponse(syntactical,
                                     validity,
                                     quality,
                                     `macro`,
                                     SubmissionStatus.valueOf(edits.statusCode))
              complete(ToResponseMarshallable(editsSummaryResponse))
            case Failure(e) =>
              failedResponse(StatusCodes.InternalServerError, uri, e)
          }
        }
    }

  def editsRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          editsSummaryPath
        }
      }
    }
  }

  private def toEditSummaryResponse(e: EditSummary): EditSummaryResponse = {
    EditSummaryResponse(e.editName, description(e.editName))
  }

  private def description(editName: String): String = ""

}
