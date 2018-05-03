## Submission status

Every submission will store a `SubmissionStatus` object with the following structure:

* `code`: `Integer`
* `message`: `String`
* `description`: `String`

For example:

```json
{
  "id": 3,
  "status": {
    "code": 1,
    "message": "No data has been uploaded yet.",
    "description": "The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file."
  }
}
```

In order to track the status of a filing for a financial institution, the following states are captured by the backend:

* `1`: `No data has been uploaded yet.` - The filing period is open and available to accept HMDA data. Make sure your data is in a pipe-delimited text file.
* `2`: `Your file is uploading.` - Your file is currently being uploaded to the HMDA Platform.
* `3`: `Your file has been uploaded.` - Your data is ready to be analyzed.
* `4`: `Checking the formatting of your data.` - Your file is being analyzed to ensure that it meets formatting requirements specified in the HMDA Filing Instructions Guide.
* `5`: `Your data has formatting errors.` - Review these errors and update your file. Then, upload the corrected file.
* `6`: `Your data is formatted correctly.` - Your file meets the formatting requirements specified in the HMDA Filing Instructions Guide. Your data will now be analyzed for any edits.
* `7`: `Your data is being analyzed.` - Your data has been uploaded and is being checked for any edits.
* `8`: `Your data has edits that need to be reviewed.` - Your file has been uploaded, but the filing process may not proceed until edits are verified or the file is corrected and re-uploaded.
* `9`: `Your data is ready for submission.` - Your financial institution has certified that the data is correct, but it has not been submitted yet.
* `10`: `Your submission has been accepted. ` - This completes your HMDA filing process for this year. If you need to upload a new HMDA file, the previously completed filing will not be overridden until all edits have been cleared and verified, and the new file has been submitted.
* `-1`: `An error occurred while submitting the data.` - Please re-upload your file.
