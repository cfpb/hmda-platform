package hmda.model.filing.submission

case class Submission(
    id: SubmissionId = SubmissionId(),
    status: SubmissionStatus = Created,
    start: Long = 0,
    end: Long = 0,
    fileName: String = "",
    receipt: String = ""
) {
  def isEmpty: Boolean =
    id == SubmissionId() && status == Created && start == 0 && end == 0 && fileName == "" && receipt == ""
}
