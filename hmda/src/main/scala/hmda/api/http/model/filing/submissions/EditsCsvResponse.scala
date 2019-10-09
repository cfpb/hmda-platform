package hmda.api.http.model.filing.submissions

case class EditsCsvResponse(editType: String, editName: String, uli: String, editDescription: String) {
  def toCsv: String = s"$editType,$editName,$uli,$editDescription\n"
}
