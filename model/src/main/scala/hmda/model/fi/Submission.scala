package hmda.model.fi

import SubmissionStatusMessage._

sealed trait SubmissionStatus {
  def code: Int
  def message: String
}

case object Created extends SubmissionStatus {
  override def code: Int = 1
  override def message: String = createdMsg
}
case object Uploading extends SubmissionStatus {
  override def code: Int = 2
  override def message: String = uploadingMsg
}
case object Uploaded extends SubmissionStatus {
  override def code: Int = 3
  override def message: String = uploadedMsg
}
case object Parsing extends SubmissionStatus {
  override def code: Int = 4
  override def message: String = parsingMsg
}
case object Parsed extends SubmissionStatus {
  override def code: Int = 5
  override def message: String = parsedMsg
}
case object ParsedWithErrors extends SubmissionStatus {
  override def code: Int = 6
  override def message: String = parsedWithErrorsMsg
}
case object Validating extends SubmissionStatus {
  override def code: Int = 7
  override def message: String = validatingMsg
}
case object ValidatedWithErrors extends SubmissionStatus {
  override def code: Int = 8
  override def message: String = validatedWithErrorsMsg
}
case object Validated extends SubmissionStatus {
  override def code: Int = 9
  override def message: String = validatedMsg
}
case object IRSGenerated extends SubmissionStatus {
  override def code: Int = 10
  override def message: String = iRSGeneratedMsg
}
case object IRSVerified extends SubmissionStatus {
  override def code: Int = 11
  override def message: String = iRSVerifiedMsg
}
case object Signed extends SubmissionStatus {
  override def code: Int = 12
  override def message: String = signedMsg
}
case class Failed(message: String) extends SubmissionStatus {
  override def code: Int = -1
}

case class SubmissionId(institutionId: String, period: String, sequenceNumber: Int) {
  override def toString: String = s"$institutionId-$period-$sequenceNumber"
}

case class Submission(submissionId: SubmissionId = SubmissionId("", "", 0), submissionStatus: SubmissionStatus = Created)
