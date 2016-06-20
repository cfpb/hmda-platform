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
case object ValidatingSyntaxAndValidity extends SubmissionStatus {
  override def code: Int = 6
  override def message: String = "validating syntax and validity"
}
case object ValidatedSyntaxAndValidity extends SubmissionStatus {
  override def code: Int = 7
  override def message: String = "validated syntax and validity"
}
case object ValidatingQualityAndMacro extends SubmissionStatus {
  override def code: Int = 8
  override def message: String = "validating quality and macro"
}
case object Unverified extends SubmissionStatus {
  override def code: Int = 9
  override def message: String = "verification required for quality and macro"
}
case object Verified extends SubmissionStatus {
  override def code: Int = 10
  override def message: String = "all edits have been verified"
}
case object Signed extends SubmissionStatus {
  override def code: Int = 11
  override def message: String = "submission complete"
}
case class Failed(message: String) extends SubmissionStatus {
  override def code: Int = -1
}

case class Submission(id: Int = 0, submissionStatus: SubmissionStatus = Created)
