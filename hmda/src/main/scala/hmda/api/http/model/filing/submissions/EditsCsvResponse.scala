package hmda.api.http.model.filing.submissions

case class EditsCsvResponse(editType: String, editName: String, uli: String) {
  def toCsv: String = s"$editType,$editName,$uli\n"
}
