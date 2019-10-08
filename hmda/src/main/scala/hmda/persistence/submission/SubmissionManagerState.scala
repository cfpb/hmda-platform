package hmda.persistence.submission

import hmda.model.filing.submission.{ Created, SubmissionStatus }

case class SubmissionManagerState(submissionStatus: SubmissionStatus = Created)
