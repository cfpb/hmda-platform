package hmda.api.http.model.filing.submissions

import hmda.model.validation.ValidationError

case class ValidationErrorSummary(parserErrors: Seq[HmdaRowParsedErrorSummary],
                                   validationErrors: Seq[Option[List[ValidationError]]])