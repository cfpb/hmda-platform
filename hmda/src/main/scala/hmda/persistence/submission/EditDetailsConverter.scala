package hmda.persistence.submission

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.edits.{EditDetail, EditDetailRow, FieldDetail}
import hmda.model.validation.ValidationError

object EditDetailsConverter {

  def validatedRowToEditDetails(
      hmdaRowValidatedError: HmdaRowValidatedError): Iterable[EditDetail] = {

    val validationErrors = hmdaRowValidatedError.validationErrors
    val vEditsMap: Map[String, List[ValidationError]] =
      validationErrors.groupBy(_.editName)
    vEditsMap.map(x => validationErrorsToEditDetail(x._1, x._2))
  }

  private def validationErrorsToEditDetail(
      editName: String,
      errors: List[ValidationError]): EditDetail = {

    val editDetailRows = errors
      .map(e => EditDetailRow(e.uli, validationErrorToFieldDetails(e)))

    EditDetail(
      editName,
      editDetailRows
    )
  }

  //TODO: perform mapping Validation Error --> Field Details (name, value)
  private def validationErrorToFieldDetails(
      validationError: ValidationError): Seq[FieldDetail] =
    Seq(
      FieldDetail("name", 1)
    )

}
