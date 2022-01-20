package hmda.api.ws.model

import hmda.model.filing.submission.SubmissionStatus

case class SubmissionStatusWSResponse(status: SubmissionStatus, messageType: String) extends WSResponseType
