package hmda.persistence.demo

import hmda.model.institution._
import hmda.model.ResourceUtils
import hmda.parser.fi.InstitutionParser

object DemoInstitutions extends ResourceUtils {

  val values: Set[Institution] = {
    val lines = resourceLines("/demoInstitutions.csv")

    lines.tail.map { line =>
      InstitutionParser(line)
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

}
