package hmda.institution.query

case class InstitutionNoteHistoryEntity(
  id: Int = 0,
  year: String = "",
  lei: String = "",
  historyID: String ="",
  notes: String = "",
  updatedPanel: String = ""
)
