package hmda.api.model

import hmda.model.fi.SubmissionStatus
import hmda.validation.engine.MacroEditJustification
import spray.json.JsObject

case class RowId(rowId: String)
case class EditResultRow(row: RowId, fields: JsObject)

// For an individual edit, all of the rows that failed it
case class EditResult(edit: String, description: String, rows: Seq[EditResultRow]) {
  def toCsv(editType: String) = {
    rows.map(r => Seq(editType, edit, r.row.rowId).mkString("", ", ", "\n")).mkString
  }
}
case class EditResults(edits: Seq[EditResult]) extends EditResultsCollection
case class QualityEditResults(verified: Boolean, edits: Seq[EditResult]) extends EditResultsCollection
trait EditResultsCollection {
  def edits: Seq[EditResult]
  def toCsv(editType: String) = edits.map(e => e.toCsv(editType)).mkString
}

case class QualityEditsVerification(verified: Boolean)
case class QualityEditsVerifiedResponse(verified: Boolean, status: SubmissionStatus)

case class MacroResult(edit: String, justifications: Set[MacroEditJustification])
case class MacroResults(edits: Seq[MacroResult]) {
  def toCsv = edits.map(e => "macro, " + e.edit + "\n").mkString
}
case object MacroResults {
  def empty: MacroResults = MacroResults(Nil)
}
case class SummaryEditResults(
    syntactical: EditResults,
    validity: EditResults,
    quality: QualityEditResults,
    `macro`: MacroResults
) {
  def toCsv = {
    val s = syntactical.toCsv("syntactical")
    val v = validity.toCsv("validity")
    val q = quality.toCsv("quality")
    "editType, editId, loanId\n" + s + v + q + `macro`.toCsv
  }
}

