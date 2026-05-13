package hmda.publisher.query.submissionhistory

case class SubmissionHistoryEntity(
    lei: String,
    submissionId: String,
    signDate: Option[Long] = Some(0L)
)
