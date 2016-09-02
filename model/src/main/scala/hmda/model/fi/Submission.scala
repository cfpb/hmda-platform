package hmda.model.fi

sealed trait SubmissionStatus {
  def code: Int
  def message: String
}

case object Created extends SubmissionStatus {
  override def code: Int = 1
  override def message: String = "created"
}
case object Uploading extends SubmissionStatus {
  override def code: Int = 2
  override def message: String = "uploading"
}
case object Uploaded extends SubmissionStatus {
  override def code: Int = 3
  override def message: String = "uploaded"
}
case object Parsing extends SubmissionStatus {
  override def code: Int = 4
  override def message: String = "parsing"
}
case object Parsed extends SubmissionStatus {
  override def code: Int = 5
  override def message: String = "parsed"
}
case object Validating extends SubmissionStatus {
  override def code: Int = 6
  override def message: String = "validating syntactical and validity"
}
case object Validated extends SubmissionStatus {
  override def code: Int = 7
  override def message: String = "validated syntactical and validity"
}
case object ValidatedWithErrors extends SubmissionStatus {
  override def code: Int = 8
  override def message: String = "validating quality and macro"
}
case object Signed extends SubmissionStatus {
  override def code: Int = 9
  override def message: String = "signed"
}
case class Failed(message: String) extends SubmissionStatus {
  override def code: Int = -1
}

case class Submission(id: Int = 0, submissionStatus: SubmissionStatus = Created)
