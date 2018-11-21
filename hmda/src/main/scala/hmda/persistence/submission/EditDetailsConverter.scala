package hmda.persistence.submission

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.edits.{EditDetails, EditDetailsRow, FieldDetails}
import hmda.model.validation.ValidationError

object EditDetailsConverter {

  def validatedRowToEditDetails(
      hmdaRowValidatedError: HmdaRowValidatedError): Iterable[EditDetails] = {

    val validationErrors = hmdaRowValidatedError.validationErrors
    val vEditsMap: Map[String, List[ValidationError]] =
      validationErrors.groupBy(_.editName)
    vEditsMap.map(x => validationErrorsToEditDetail(x._1, x._2))
  }

  private def validationErrorsToEditDetail(
      editName: String,
      errors: List[ValidationError]): EditDetails = {

    val editDetailRows = errors
      .map(e => EditDetailsRow(e.uli, validationErrorToFieldDetails(e)))

    EditDetails(
      editName,
      editDetailRows
    )
  }

  //TODO: perform mapping Validation Error --> Field Details (name, value)
  private def validationErrorToFieldDetails(
      validationError: ValidationError): Seq[FieldDetails] =
    Seq(
      FieldDetails("name", 1)
    )

}
