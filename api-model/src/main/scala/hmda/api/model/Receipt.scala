package hmda.api.model

import hmda.model.fi.SubmissionStatus

case class Receipt(timestamp: Long, receipt: String, status: SubmissionStatus)
