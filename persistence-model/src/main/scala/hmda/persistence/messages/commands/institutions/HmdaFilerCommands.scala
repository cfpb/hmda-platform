package hmda.persistence.messages.commands.institutions

import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.CommonMessages.Command

object HmdaFilerCommands {

  case class CreateHmdaFiler(hmdaFiler: HmdaFiler) extends Command
  case class DeleteHmdaFiler(hmdaFiler: HmdaFiler) extends Command
  case class FindHmdaFiler(institutionId: String) extends Command
}
