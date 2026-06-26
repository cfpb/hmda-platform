package hmda.institution.api.http

import hmda.institution.query.{InstitutionEmailEntity, InstitutionEntity}
import hmda.model.institution._
import hmda.util.CSVConsolidator.listDeDupeToList

object InstitutionConverter {

  def convert(entity: InstitutionEntity, emails: Seq[String]): Institution = {
    Institution(
      entity.activityYear,
      entity.lei,
      Agency.valueOf(entity.agency),
      "",
      "",
      if (entity.taxId != "") Some(entity.taxId) else None,
      entity.rssd,
      emails,
      Respondent(
        if (entity.respondentName != "") Some(entity.respondentName) else None,
        if (entity.respondentState != "") Some(entity.respondentState)
        else None,
        if (entity.respondentCity != "") Some(entity.respondentCity) else None
      ),
      Parent(
       "",
       ""
      ),
      entity.assets,
      entity.otherLenderCode,
      TopHolder("", ""),
      entity.hmdaFiler,
      entity.quarterlyFiler,
      entity.quarterlyFilerHasFiledQ1,
      entity.quarterlyFilerHasFiledQ2,
      entity.quarterlyFilerHasFiledQ3,
      entity.notes
    )
  }

  def convert(institution: Institution): InstitutionEntity =
    InstitutionEntity(
      institution.LEI,
      institution.activityYear,
      institution.agency.code,
      "",
      "",
      institution.taxId.getOrElse(""),
      institution.rssd,
      institution.respondent.name.getOrElse(""),
      institution.respondent.state.getOrElse(""),
      institution.respondent.city.getOrElse(""),
      institution.parent.idRssd,
      "",
      institution.assets,
      institution.otherLenderCode,
      institution.topHolder.idRssd,
      "",
      institution.hmdaFiler,
      institution.quarterlyFiler,
      institution.quarterlyFilerHasFiledQ1,
      institution.quarterlyFilerHasFiledQ2,
      institution.quarterlyFilerHasFiledQ3,
      notes = institution.notes
    )

  def emailsFromInstitution(institution: Institution): Seq[InstitutionEmailEntity] = {

    val uniqueEmailDomainList= listDeDupeToList(institution.emailDomains)
    uniqueEmailDomainList.map(email => InstitutionEmailEntity(lei = institution.LEI, emailDomain = email))
  }
}
