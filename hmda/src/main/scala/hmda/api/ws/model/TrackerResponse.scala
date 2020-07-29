package hmda.api.ws.model

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

    Json.arr(
      Json.fromString("ValidationProgressTracker"),
      Json.obj(
        "lei" := t.submissionId.lei,
        "submission" := t.submissionId.toString,
        "syntactical" := stringFriendlyProgress(t.state.syntacticalValidation),
        "quality" := stringFriendlyProgress(t.state.qualityValidation),
        "macro" := stringFriendlyProgress(t.state.macroValidation)
      )
    )
  }
}
final case class TrackerResponse(submissionId: SubmissionId, state: ValidationProgressTrackerState)