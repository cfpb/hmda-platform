package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus

case class EditsVerificationResponse(verified: Boolean,
                                     status: SubmissionStatus)
