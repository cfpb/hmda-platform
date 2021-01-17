package hmda.submissionerrors.repositories

import hmda.model.filing.submission.SubmissionId
import monix.eval.Task
// $COVERAGE-OFF$
trait SubmissionErrorRepository {

  /**
   * Determines if a submission is already present in the repository
   * @param submissionId is the submission ID that consists of LEI + Period + Sequence Number
   * @return a Boolean (true if it exists/false if it does not)
   */
  def submissionPresent(submissionId: SubmissionId): Task[Boolean]

  /**
   * Inserts error-data pertaining to a submission. This also handles the case if a submission already exists and
   * in that case, a no-op will be done
   *
   * @param submissionId is the Submission ID that consists of LEI + Period + Sequence Number
   * @param submissionStatus is the submission status code
   * @param info is a list of error information to be added for a given Submission ID
   * @return
   */
  def add(submissionId: SubmissionId, submissionStatus: Int, info: List[AddSubmissionError]): Task[Unit]
}
// $COVERAGE-ON$