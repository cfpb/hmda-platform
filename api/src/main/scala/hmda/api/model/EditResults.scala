package hmda.api.model

import hmda.validation.engine.MacroEditJustification

case class LarId(loanId: String)
case class LarEditResult(lar: LarId)

// For an individual edit, all of the rows that failed it
case class EditResult(edit: String, description: String, ts: Boolean, lars: Seq[LarEditResult]) {
  def toCsv(editType: String) = {
    val larCsv = lars.map(l => Seq(editType, edit, l.lar.loanId).mkString("", ", ", "\n")).mkString
    if (ts) editType + ", " + edit + ", " + "Transmittal Sheet\n" + larCsv
    else larCsv
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
case class RowResults(rows: Seq[RowResult], macroResults: MacroResults)
case class RowEditDetail(editId: String, description: String)

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

