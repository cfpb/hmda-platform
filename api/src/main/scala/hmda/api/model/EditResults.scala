package hmda.api.model

import hmda.model.fi.SubmissionStatus
import spray.json.JsObject

case class RowId(rowId: String)
case class EditResultRow(row: RowId, fields: JsObject)

// For an individual edit, all of the rows that failed it
case class EditResult(edit: String, description: String, rows: Seq[EditResultRow]) {
  def toCsv(editType: String) = {
    rows.map(r => Seq(editType, edit, r.row.rowId).mkString("", ", ", "\n")).mkString
  }
}

case class EditsVerification(verified: Boolean)
case class EditsVerifiedResponse(verified: Boolean, status: SubmissionStatus)

case class EditInfo(edit: String, description: String)
case class EditCollection(edits: Seq[EditInfo])
case class VerifiableEditCollection(verified: Boolean, edits: Seq[EditInfo])

case class SummaryEditResults(
  syntactical: EditCollection,
  validity: EditCollection,
  quality: VerifiableEditCollection,
  `macro`: VerifiableEditCollection,
  status: SubmissionStatus
)
case class SingleTypeEditResults(edits: Seq[EditInfo], status: SubmissionStatus)
