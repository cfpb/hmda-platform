package hmda.model.filing.submission

case class Submission(
    id: SubmissionId = SubmissionId(),
    status: SubmissionStatus = Created,
    start: Long = 0,
    end: Long = 0,
    fileName: String = "",
    receipt: String = ""
) {

  override def toString: String = "This is a test"

  def isEmpty: Boolean =
    id == SubmissionId() && status == Created && start == 0 && end == 0 && fileName == "" && receipt == ""
}
