package hmda.api.http.model.filing.submissions

import scala.collection.immutable._

case class SingleValidationErrorSummary(
  uli: String,
  editName: String,
  editDescription: String,
  fields: ListMap[String, String]
)

case class ValidationErrorSummary(parserErrors: Seq[HmdaRowParsedErrorSummary],
                                   validationErrors: Seq[List[SingleValidationErrorSummary]])
