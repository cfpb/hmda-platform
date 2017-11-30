package hmda.persistence.messages.commands.institutions

import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages.Command

object InstitutionCommands {
  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class GetInstitutionByRespondentId(id: String) extends Command
}
