package hmda.submissionerrors.repositories

import hmda.model.filing.submission.SubmissionId
import monix.eval.Task

trait SubmissionErrorRepository {
  def submissionPresent(submissionId: SubmissionId): Task[Boolean]

  def add(submissionId: SubmissionId, submissionStatus: Int, info: List[AddSubmissionError]): Task[Unit]
}