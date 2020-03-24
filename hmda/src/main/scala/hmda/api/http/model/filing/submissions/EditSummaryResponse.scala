package hmda.api.http.model.filing.submissions

import io.circe._
import io.circe.generic.semiauto._

case class EditSummaryResponse(edit: String, description: String, transmittalSheet: Boolean)
object EditSummaryResponse {
  implicit val encoder: Encoder[EditSummaryResponse] =
    deriveEncoder[EditSummaryResponse]

  implicit val ordering: Ordering[EditSummaryResponse] =
    (x: EditSummaryResponse, y: EditSummaryResponse) => {

      val xName =  x.edit
      val yName =y.edit

      xName compare yName
    }
}

case class ValidityEditSummaryResponse(edits: Seq[EditSummaryResponse])
object ValidityEditSummaryResponse {
  implicit val encoder: Encoder[ValidityEditSummaryResponse] =
    deriveEncoder[ValidityEditSummaryResponse]
}

case class QualityEditSummaryResponse(edits: Seq[EditSummaryResponse],
                                      verified: Boolean)
object QualityEditSummaryResponse {
  implicit val encoder: Encoder[QualityEditSummaryResponse] =
    deriveEncoder[QualityEditSummaryResponse]
}

case class MacroEditSummaryResponse(edits: Seq[EditSummaryResponse],
                                    verified: Boolean)
object MacroEditSummaryResponse {
  implicit val encoder: Encoder[MacroEditSummaryResponse] =
    deriveEncoder[MacroEditSummaryResponse]
}

case class SyntacticalEditSummaryResponse(edits: Seq[EditSummaryResponse])
object SyntacticalEditSummaryResponse {
  implicit val encoder: Encoder[SyntacticalEditSummaryResponse] =
    deriveEncoder[SyntacticalEditSummaryResponse]
}

case class EditsSummaryResponse(syntactical: SyntacticalEditSummaryResponse,
                                validity: ValidityEditSummaryResponse,
                                quality: QualityEditSummaryResponse,
                                `macro`: MacroEditSummaryResponse,
                                status: SubmissionStatusResponse)
object EditsSummaryResponse {
  implicit val encoder: Encoder[EditsSummaryResponse] =
    deriveEncoder[EditsSummaryResponse]
}