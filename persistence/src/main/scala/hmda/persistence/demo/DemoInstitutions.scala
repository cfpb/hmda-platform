package hmda.persistence.demo

import hmda.model.institution._
import hmda.model.ResourceUtils

object DemoInstitutions extends ResourceUtils {

  val values: Set[Institution] = {
    val lines = resourceLines("/demoInstitutions.csv")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val id = values(0)
      val name = values(1)
      val externalId = values(2)
      val externalIdType = toExternalIdType(values(3))
      val agency = toAgency(values(4))
      val institution = toInstitutionType(values(5))
      val parent = values(6).toBoolean
      val status = toStatus(values(7))

      Institution(
        id,
        name,
        Set(ExternalId(externalId, externalIdType)),
        agency,
        institution,
        parent,
        status
      )
    }.toSet
  }

  def toInstitutionType(text: String): InstitutionType = {
    InstitutionType.values.filter(inst => inst.entryName == text).head
  }

  def toExternalIdType(text: String): ExternalIdType = {
    ExternalIdType.values.filter(id => id.entryName == text).head
  }

  def toAgency(text: String): Agency = {
    Agency.values.filter(agency => agency.name == text).head
  }

  def toStatus(text: String): InstitutionStatus = {
    InstitutionStatus.values.filter(status => status.entryName == text).head
  }

}
