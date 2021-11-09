package hmda.api.ws.model
// $COVERAGE-OFF$
import hmda.model.filing.submission.SubmissionId
import hmda.model.processing.state.{ ValidationProgress, ValidationProgressTrackerState }
import io.circe.{ Encoder, Json }
import io.circe.syntax._

/**
 * TrackerResponses are used by the WebSocketProgressTracker to send JSON responses back to the user over the websocket
 * when progress tracking for a particular submission id is requested
 */
object TrackerResponse {
  implicit val trackerResponseEncoder: Encoder[TrackerResponse] = { t: TrackerResponse =>
    def stringFriendlyProgress(p: ValidationProgress): String = p match {
      case ValidationProgress.Waiting                => "Waiting"
      case ValidationProgress.InProgress(percentage) => s"InProgress($percentage)"
      case ValidationProgress.Completed              => s"Completed"
      case ValidationProgress.CompletedWithErrors    => s"CompletedWithErrors"
    }

    def edits(e: Set[String]): Json = Json.arr(e.toSeq.map(Json.fromString): _*)

    Json.arr(
      Json.fromString("ValidationProgressTracker"),
      Json.obj(
        "lei" := t.submissionId.lei,
        "submission" := t.submissionId.toString,
        "syntactical" := stringFriendlyProgress(t.state.syntacticalValidation),
        "syntactical-errors" := edits(t.state.syntacticalEdits),
        "quality" := stringFriendlyProgress(t.state.qualityValidation),
        "quality-errors" := edits(t.state.qualityEdits),
        "macro" := stringFriendlyProgress(t.state.macroValidation),
        "macro-errors" := edits(t.state.macroEdits),
      )
    )
  }
}
final case class TrackerResponse(submissionId: SubmissionId, state: ValidationProgressTrackerState)
// $COVERAGE-ON$