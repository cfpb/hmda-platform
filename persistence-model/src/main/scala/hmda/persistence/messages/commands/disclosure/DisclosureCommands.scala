package hmda.persistence.messages.commands.disclosure

import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.Command

object DisclosureCommands {
  case class GenerateDisclosureReports(submissionId: SubmissionId) extends Command
}
