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
    "message": "created",
    "description": "The filing period is now open and available to accept HMDA data."
  }
}
```

In order to track the status of a filing for a financial institution, the following states are captured by the backend:

* `1`: `created` - The filing period is now open and available to accept HMDA data.
* `2`: `uploading` - The data are currently being uploaded to the HMDA Platform.
* `3`: `uploaded` - The data have finished uploading and are ready to be analyzed.
* `4`: `parsing` - The data are currently being analyzed to ensure that they meet certain formatting requirements specified in the HMDA Filing Instructions Guide.
* `5`: `parsed with errors` - The data are not formatted according to certain formatting requirements specified in the Filing Instructions Guide. The filing process may not proceed until the data have been corrected and the file has been reuploaded.
* `6`: `parsed` - The data conforms to certain formatting requirements specified in the Filing Instructions Guide. The filing process will now proceed.
* `7`: `validating` - The data are currently being validated.
* `8`: `validated with errors` - The data validation process is complete, but there are edits that need to be addressed. The filing process may not proceed until the file has been corrected and reuploaded.
* `9`: `validated` - The data validation process is complete and the data are ready to be submitted.
* `10`: `signed` - Your financial institution has certified that the data is correct. This completes the HMDA filing process for this year.
* `-1`: `Failed` - An error occurred during the process of submitting the data. Please re-upload your file.
