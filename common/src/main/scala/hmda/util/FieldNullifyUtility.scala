package hmda.util

import hmda.model.institution.{Institution, InstitutionType, Parent, Respondent, TopHolder}


object FieldNullifyUtility {
  def nullifyInstitutionFields(institution: Institution): Institution = {
      //HMDA Ops will no longer provide updates to institution fields if they are not shared
      //with the transmittal sheet model
      if( institution.activityYear>=2024)
{
      Institution(
        institution.activityYear,
        institution.LEI,
        institution.agency,
        InstitutionType.valueOf(-1),
        institution.institutionId_2017,
        institution.taxId,
        -1,
        institution.emailDomains,
        institution.respondent,
        Parent(-1,Some("")),
        -1,
        -1,
        TopHolder(-1,Some("")),
        institution.hmdaFiler,
        institution.quarterlyFiler,
        institution.quarterlyFilerHasFiledQ1,
        institution.quarterlyFilerHasFiledQ2,
        institution.quarterlyFilerHasFiledQ3,
        institution.notes
      )}
      else
        institution
  }
}
