package hmda.api.model

import hmda.validation.engine.MacroEditJustification
import spray.json.JsObject

case class RowId(rowId: String)
case class EditResultRow(row: RowId)

// For an individual edit, all of the rows that failed it
case class EditResult(edit: String, description: String, rows: Seq[EditResultRow]) {
  def toCsv(editType: String) = {
    rows.map(r => Seq(editType, edit, r.row.rowId).mkString("", ", ", "\n")).mkString
  }
}
case class EditResults(edits: Seq[EditResult]) {
  def toCsv(editType: String) = edits.map(e => e.toCsv(editType)).mkString
}
case object EditResults {
  def empty: EditResults = EditResults(Nil)
}

// For a single row, all of the edits that it failed
case class RowResult(rowId: String, edits: Seq[RowEditDetail])
case class RowResults(rows: Seq[RowResult], `macro`: MacroResults)
case class RowEditDetail(editId: String, description: String, fields: JsObject)

case class MacroResult(edit: String, justifications: Set[MacroEditJustification])
case class MacroResults(edits: Seq[MacroResult]) {
  def toCsv = edits.map(e => "macro, " + e.edit + "\n").mkString
}
case object MacroResults {
  def empty: MacroResults = MacroResults(Nil)
}
case class SummaryEditResults(syntactical: EditResults, validity: EditResults, quality: EditResults, `macro`: MacroResults) {
  def toCsv = {
    val s = syntactical.toCsv("syntactical")
    val v = validity.toCsv("validity")
    val q = quality.toCsv("quality")
    "editType, editId, loanId\n" + s + v + q + `macro`.toCsv
  }
}

