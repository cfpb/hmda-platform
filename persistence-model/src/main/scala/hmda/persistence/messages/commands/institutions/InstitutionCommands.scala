package hmda.persistence.messages.commands.institutions

import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages.Command

object InstitutionCommands {
  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class DeleteInstitution(i: Institution) extends Command
  case class GetInstitutionByRespondentId(id: String) extends Command
  case class GetInstitutionById(institutionId: String) extends Command
  case class GetInstitutionsById(ids: List[String]) extends Command
  case class FindInstitutionByDomain(domain: String) extends Command
}
