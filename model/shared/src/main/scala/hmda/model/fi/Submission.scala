package hmda.model.fi

import SubmissionStatusMessage._

sealed trait SubmissionStatus {
  def code: Int
  def message: String
  def description: String
}

case object Created extends SubmissionStatus {
  override def code: Int = 1
  override def message: String = createdMsg
  override def description: String = createdDescription
}
case object Uploading extends SubmissionStatus {
  override def code: Int = 2
  override def message: String = uploadingMsg
  override def description: String = uploadingDescription
}
case object Uploaded extends SubmissionStatus {
  override def code: Int = 3
  override def message: String = uploadedMsg
  override def description: String = uploadedDescription
}
case object Parsing extends SubmissionStatus {
  override def code: Int = 4
  override def message: String = parsingMsg
  override def description: String = parsingDescription
}
case object ParsedWithErrors extends SubmissionStatus {
  override def code: Int = 5
  override def message: String = parsedWithErrorsMsg
  override def description: String = parsedWithErrorsDescription
}
case object Parsed extends SubmissionStatus {
  override def code: Int = 6
  override def message: String = parsedMsg
  override def description: String = parsedDescription
}
case object Validating extends SubmissionStatus {
  override def code: Int = 7
  override def message: String = validatingMsg
  override def description: String = validatingDescription
}
case object ValidatedWithErrors extends SubmissionStatus {
  override def code: Int = 8
  override def message: String = validatedWithErrorsMsg
  override def description: String = validatedWithErrorsDescription
}
case object Validated extends SubmissionStatus {
  override def code: Int = 9
  override def message: String = validatedMsg
  override def description: String = validatedDescription
}
case object Signed extends SubmissionStatus {
  override def code: Int = 10
  override def message: String = signedMsg
  override def description: String = signedDescription
}
case class Failed(message: String) extends SubmissionStatus {
  override def code: Int = -1
  override def description: String = failedDescription
}

case class SubmissionId(institutionId: String = "", period: String = "", sequenceNumber: Int = 0) {
  override def toString: String = s"$institutionId-$period-$sequenceNumber"
}

case class Submission(
  id: SubmissionId = SubmissionId(),
  status: SubmissionStatus = Created,
  start: Long = 0L,
  end: Long = 0L,
  receipt: String = ""
)
