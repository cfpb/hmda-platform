package hmda.institution.util

import hmda.institution.query.InstitutionEntity
import hmda.model.institution.{Institution, Respondent}


object HelperUtility {
  def nullifyInstitutionFields(institution: Institution): Institution = {

      Institution(
        institution.activityYear,
        institution.LEI,
        institution.agency,
        null,
        Some(""),
        institution.taxId,
        -1,
        institution.emailDomains,
        Respondent(institution.respondent.name,None,None),
        null,
        -1,
        -1,
        null,
        institution.hmdaFiler,
        institution.quarterlyFiler,
        institution.quarterlyFilerHasFiledQ1,
        institution.quarterlyFilerHasFiledQ2,
        institution.quarterlyFilerHasFiledQ3,
        institution.notes
      )
  }

  def nullifyInstitutionEntityFields(institutionEntity: InstitutionEntity): InstitutionEntity = {

    InstitutionEntity(
      institutionEntity.lei,
      institutionEntity.activityYear,
      institutionEntity.agency,
      -1,
      "",
      institutionEntity.taxId,
      -1,
      institutionEntity.respondentName,
      "",
      "",
      -1,
      "",
      -1,
      -1,
      -1,
      "",
      institutionEntity.hmdaFiler,
      institutionEntity.quarterlyFiler,
      institutionEntity.quarterlyFilerHasFiledQ1,
      institutionEntity.quarterlyFilerHasFiledQ2,
      institutionEntity.quarterlyFilerHasFiledQ3
    )
  }
}
