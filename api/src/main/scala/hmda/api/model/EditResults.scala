package hmda.api.model

import hmda.model.fi.SubmissionStatus
import spray.json.JsObject

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

case class EditResult(
  edit: String,
  rows: Seq[EditResultRow],
  path: String,
  currentPage: Int,
  total: Int
) extends PaginatedResponse

case class RowId(rowId: String)
case class EditResultRow(row: RowId, fields: JsObject)

/* When converted to JSON, EditResult has this format:

EditResult(
  edit: String,
  rows: Seq[EditResultRow],
  count: Int,
  total: Int,
  _links: PaginatedLinks)

This happens in EditResultsProtocol.scala */
