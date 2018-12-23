package hmda.regulator.query.panel

import hmda.model.institution.Institution

object InstitutionConverter {
  def convert(institution: Institution): InstitutionEntity = {
    InstitutionEntity(
      institution.LEI,
      institution.activityYear,
      institution.agency.code,
      institution.institutionType.code,
      institution.institutionId_2017.getOrElse(""),
      institution.taxId.getOrElse(""),
      institution.rssd,
      institution.respondent.name.getOrElse(""),
      institution.respondent.state.getOrElse(""),
      institution.respondent.city.getOrElse(""),
      institution.parent.idRssd,
      institution.parent.name.getOrElse(""),
      institution.assets,
      institution.otherLenderCode,
      institution.topHolder.idRssd,
      institution.topHolder.name.getOrElse(""),
      institution.hmdaFiler
    )
  }
}
