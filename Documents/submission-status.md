## Submission status

Every submission will store a `SubmissionStatus` object with the following structure:

* `code`: `Integer`
* `message`: `String`

For example:

```json
{
  "id": 3,
  "status": {
    "code": 1,
    "message": "created"
  }
}
```

In order to track the status of a filing for a financial institution, the following states are captured by the backend:

* `1`: `Created` - The submission for a filing period has been created and is ready to accept data.
* `2`: `Uploading` - Data is currently being uploaded to the system.
* `3`: `Uploaded` - Data has finished uploading, is stored in the `HMDA Platform`, and is ready to be parsed.
* `4`: `Parsing` - The submitted data is being checked for parsing errors according to the [HMDA File Specification](2017_File_Spec_LAR.csv).
* `5`: `ParsedWithErrors` - The data is incorrectly formatted and requires resubmission. No syntactical, validity, quality, or macro edit checks will be performed.
* `6`: `Parsed` - The data conforms to the requirements and is ready to have the syntactical, validity, quality, and macro edit checks performed by the rules engine.
* `7`: `Validating` - Submitted data is being run through the rules engine that checks for syntactical, validity, quality, and macro edits. All edit checks are performed in this state.
* `8`: `ValidatedWithErrors` - The validation process is complete but there are edits (errors) in the provided data. These edits could consist of any combination of syntactical, validity, quality, and macro edits. If syntactical or validity edits exist a resubmission of the data is required. If quality or macro edits exist they will need verification before moving to the next state.
* `9`: `Validated` - The validation process is complete and the data submitted passes all syntactical and validity edits and all quality and macro edits, if they existed, have been verified. The data is now considered valid and the IRS report can be generated.
* `10`: `IRSGenerated` - The IRS report has been generated.
* `11`: `IRSVerified` - The IRS report has been verified by the financial institution and the submission can be signed.
* `12`: `Signed` - The financial institution has certified that the data is correct. This completes the HMDA filing process.
* `-1`: `Failed` - An error occurred in the process of submitting data, the submission needs to be performed again.
