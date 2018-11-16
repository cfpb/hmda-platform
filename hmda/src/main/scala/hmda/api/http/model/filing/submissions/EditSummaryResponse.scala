package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus

case class EditSummaryResponse(edit: String, description: String)

case class SyntacticalEditSummaryResponse(edits: Seq[EditSummaryResponse])
case class ValidityEditSummaryResponse(edits: Seq[EditSummaryResponse])
case class QualityEditSummaryResponse(edits: Seq[EditSummaryResponse],
                                      verified: Boolean)
case class MacroEditSummaryResponse(edits: Seq[EditSummaryResponse],
                                    verified: Boolean)

case class EditsSummaryResponse(syntactical: SyntacticalEditSummaryResponse,
                                validity: ValidityEditSummaryResponse,
                                quality: QualityEditSummaryResponse,
                                `macro`: MacroEditSummaryResponse,
                                status: SubmissionStatus)
