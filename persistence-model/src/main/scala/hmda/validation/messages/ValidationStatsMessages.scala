package hmda.validation.messages

import akka.actor.ActorRef
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.Command

object ValidationStatsMessages {
  case class AddSubmissionLarStatsActorRef(actor: ActorRef, submissionId: SubmissionId) extends Command
  case class RemoveSubmissionLarStatsActorRef(submissionId: SubmissionId) extends Command
  case class FindTotalSubmittedLars(institutionId: String, period: String) extends Command
  case class FindTotalValidatedLars(institutionId: String, period: String) extends Command
  case class FindIrsStats(submissionId: SubmissionId) extends Command
  case class FindTaxId(institutionId: String, period: String) extends Command
  case class FindQ070(institutionId: String, period: String) extends Command
  case class FindQ071(institutionId: String, period: String) extends Command
  case class FindQ072(institutionId: String, period: String) extends Command
  case class FindQ075(institutionId: String, period: String) extends Command
  case class FindQ076(institutionId: String, period: String) extends Command
}
