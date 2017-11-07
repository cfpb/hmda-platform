package hmda.persistence.messages.commands.filing

import hmda.model.fi.{ Filing, FilingStatus }
import hmda.persistence.messages.CommonMessages.Command

object FilingCommands {
  case class CreateFiling(filing: Filing) extends Command
  case class UpdateFilingStatus(period: String, status: FilingStatus) extends Command
  case class GetFilingByPeriod(period: String) extends Command
}
