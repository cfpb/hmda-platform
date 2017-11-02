package hmda.persistence.messages.commands.commands

import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages.Command

object InstitutionCommands {
  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class GetInstitution(id: String) extends Command
}
