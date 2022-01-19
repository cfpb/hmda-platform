package hmda.model.filing.submission

object SubmissionStatusMessages {
  val createdMsg          = "No data has been uploaded yet."
  val uploadingMsg        = "Your file is uploading."
  val uploadedMsg         = "Your file has been uploaded."
  val parsingMsg          = "Checking the formatting of your data."
  val parsedMsg           = "Your data is formatted correctly."
  val parsedWithErrorsMsg = "Your data has formatting errors."
  val validatingMsg       = "Your data is being analyzed."
  val syntacticalOrValidityMsg =
    "Your data has been analyzed for Syntactical and Validity Errors."
  val syntacticalValidityErrorMsg =
    "Your data has syntactical and/or validity edits that need to be reviewed."
  val qualityMsg             = "Your data has been analyzed for Quality Edits."
  val qualityErrorMsg        = "Your data has quality edits that need to be reviewed."
  val validatedWithErrorsMsg = "Your data has edits that need to be reviewed."
  val macroMsg               = "Your data has been analyzed for macro errors."
  val macroErrorMsg          = "Your data has macro edits that need to be reviewed."
  val validatedMsg           = "Your data is ready for submission."
  val signedMsg              = "Your submission has been accepted."
  val failedMsg              = "An error occurred while submitting the data."

  val createdDescription =
    "The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file."
  val uploadingDescription =
    "Your file is currently being uploaded to the HMDA Platform."
  val uploadedDescription = "Your data is ready to be analyzed."
  val parsingDescription =
    "Your file is being analyzed to ensure that it meets formatting requirements specified in the HMDA Filing Instructions Guide."
  val parsedWithErrorsDescription =
    "Review these errors and update your file. Then, upload the corrected file."
  val parsedDescription =
    "Your file meets the formatting requirements specified in the HMDA Filing Instructions Guide. Your data will now be analyzed for any edits."
  val validatingDescription =
    "Your data has been uploaded and is being checked for any edits."
  val syntacticalOrValidityDescription =
    "Your file has been analyzed and does not contain any Syntactical or Validity errors."
  val syntactivalValidityErrorDescription =
    "Your file has been uploaded, but the filing process may not proceed until the file is corrected and re-uploaded."
  val qualityDescription =
    "Your file has been analyzed, and does not contain quality edits."
  val qualityErrorDescription =
    "Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded."
  val macroDescription =
    "Your file has been analyzed, and does not contain macro errors."
  val macroErrorDescription =
    "Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded."
  val validatedWithErrorsDescription =
    "Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded."
  val validatedDescription =
    "Your financial institution has certified that the data is correct, but it has not been submitted yet."
  val signedDescription =
    "This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted."
  val failedDescription = "Please re-upload your file."

  val processingMSg = "Your file is currently being processed."
}
