package hmda.messages.filing

import hmda.messages.CommonMessages.Command
import hmda.model.filing.{Filing, FilingStatus}

object FilingCommands {
  sealed trait FilingCommand extends Command
  case class CreateFiling(filing: Filing) extends FilingCommand
  case class UpdateFilingStatus(period: String, status: FilingStatus) extends FilingCommand
  case class GetFilingByPeriod(period: String) extends FilingCommand

}
