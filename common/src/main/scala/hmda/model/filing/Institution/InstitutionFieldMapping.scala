package hmda.model.filing.Institution

import hmda.model.institution.Institution

object InstitutionFieldMapping {
  def mapping(institution: Institution): Map[String, String] = Map(
    "Federal Agency" -> institution.agency.code.toString,
    "Federal Taxpayer Identification Number" -> institution.taxId.getOrElse(""),
    "Legal Entity Identifier (LEI)" -> institution.LEI
  )
}
