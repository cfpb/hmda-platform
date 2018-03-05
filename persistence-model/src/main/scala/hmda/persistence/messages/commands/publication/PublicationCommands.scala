package hmda.persistence.messages.commands.publication

import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.Command

object PublicationCommands {
  case class GenerateDisclosureReports(submissionId: SubmissionId) extends Command
  case object GenerateAggregateReports extends Command
}
