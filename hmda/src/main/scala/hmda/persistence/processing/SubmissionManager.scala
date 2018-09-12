package hmda.persistence.processing

import akka.actor.typed.Behavior
import hmda.model.filing.submission.SubmissionId

object SubmissionManager {

  final val name = "Submission"

  def initialBehavior(submissionId: SubmissionId): Behavior[String] = ???

}
