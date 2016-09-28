## Submission status

Every submission will store a `SubmissionStatus` object with the following structure:

* `code`: `Integer`
* `message`: `String`

In order to track the status of a filing for a financial institution, the following states are captured by the backend:

* `1`: `Created` - The filing has been created and is ready to accept data.
* `2`: `Uploading` - Data is currently being uploaded to the system.
* `3`: `Uploaded` - Data has finished uploading, is stored in the `HMDA Platform` and is ready to be checked.
* `4`: `Parsing` - The submitted information is being checked for parsing errors according to the [HMDA File Specification](2017_File_Spec_LAR.csv).
* `5`: `ParsedWithErrors` - Filed data is incorrectly formatted, requires resubmission.
* `6`: `Parsed` - Filed data conforms to the requirements and is ready to be validated by the rule engine.
* `7`: `Validating` - Submitted data is being run through the validation engine that checks for [Edit Checks]().
* `8`: `ValidatedWithErrors` - There are validation errors in the provided information. Depending on the type of error (syntactical and / or validity) a new submission might be necessary.
* `9`: `Validated` - Information submitted is valid and the IRS report can be generated.
* `10`: `IRSGenerated` - The IRS report has been generated.
* `11`: `IRSVerified` - The IRS report has been verified by the financial institution.
* `12`: `Signed` - The financial institution has certified that the data is correct. This finishes the HMDA filing process.
* `-1`: `Failed` - An error occurred in the process of submitting data, the submission needs to be performed again.
