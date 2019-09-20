package hmda.api.http.model.filing.submissions

import hmda.model.filing.submission.SubmissionStatus

case class SignedResponse(email: String = "test@test.com",
                          timestamp: Long,
                          receipt: String,
                          status: SubmissionStatus)
