package hmda.api.http.model.public

case class ValidatedResponse(validated: Seq[Validated])

case class Validated(lineNumber: Int, errors: String) {
  def toCSV: String = s"$lineNumber|$errors"
}
