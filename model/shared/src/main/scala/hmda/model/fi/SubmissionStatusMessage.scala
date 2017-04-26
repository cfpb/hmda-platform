package hmda.model.fi

object SubmissionStatusMessage {
  val createdMsg = "created"
  val uploadingMsg = "uploading"
  val uploadedMsg = "uploaded"
  val parsingMsg = "parsing"
  val parsedMsg = "parsed"
  val parsedWithErrorsMsg = "parsed with errors"
  val validatingMsg = "validating"
  val validatedWithErrorsMsg = "validated with errors"
  val validatedMsg = "validated"
  val signedMsg = "signed"

  val createdDescription = "The filing period is now open and available to accept HMDA data."
  val uploadingDescription = "The data are currently being uploaded to the HMDA Platform."
  val uploadedDescription = "The data have finished uploading and are ready to be analyzed."
  val parsingDescription = "The data are currently being analyzed to ensure that they meet certain formatting requirements specified in the HMDA Filing Instructions Guide."
  val parsedWithErrorsDescription = "The data are not formatted according to certain formatting requirements specified in the Filing Instructions Guide. The filing process may not proceed until the data have been corrected and the file has been reuploaded."
  val parsedDescription = "The data conforms to certain formatting requirements specified in the Filing Instructions Guide. The filing process will now proceed."
  val validatingDescription = "The data are currently being validated."
  val validatedWithErrorsDescription = "The data validation process is complete, but there are edits that need to be addressed. The filing process may not proceed until the file has been corrected and reuploaded."
  val validatedDescription = "The data validation process is complete and the data are ready to be submitted."
  val signedDescription = "Your financial institution has certified that the data is correct. This completes the HMDA filing process for this year."
  val failedDescription = "An error occurred during the process of submitting the data. Please re-upload your file."
}
