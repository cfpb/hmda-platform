package hmda.api.model

import hmda.validation.engine.MacroEditJustification

case class LarId(loanId: String)
case class LarEditResult(lar: LarId)
case class EditResult(edit: String, ts: Boolean, lars: Seq[LarEditResult])
case class EditResults(edits: Seq[EditResult])
case object EditResults {
  def empty: EditResults = EditResults(Nil)
}
case class MacroResult(edit: String, justifications: Seq[MacroEditJustification])
case class MacroResults(edits: Seq[MacroResult])
case object MacroResults {
  def empty: MacroResults = MacroResults(Nil)
}
case class SummaryEditResults(syntactical: EditResults, validity: EditResults, quality: EditResults, `macro`: MacroResults)

