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

    println(editName)
    println(errors)
    println(editDetailRows)
    EditDetails(
      editName,
      editDetailRows
    )
  }

  private def validationErrorToFieldDetails(
      validationError: ValidationError): List[FieldDetails] = {
    val fieldDetails = validationError.fields.keys.toList
      .map(fieldKey => {
        println(fieldKey)
        val fieldValue =
          validationError.fields.getOrElse(fieldKey, "Detail not found")
        val fieldValueFriendly =
          if (fieldValue == "-1") "Invalid input" else fieldValue
        val fieldDetials = FieldDetails(fieldKey, fieldValueFriendly)
        println(fieldDetials)
        fieldDetials
      })
    println("validationErrorToFieldDetails Result")
    println(fieldDetails)
    fieldDetails
  }
}
