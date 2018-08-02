package hmda.institution.api.http

import hmda.institution.query.InstitutionEntity
import hmda.model.institution._

object InstitutionConverter {

  def convert(entity: InstitutionEntity): Institution = {
    Institution(
      entity.activityYear,
      if (entity.lei != "") Some(entity.lei) else None,
      if (Agency.values.contains(entity.agency))
        Some(Agency.valueOf(entity.agency))
      else None,
      if (InstitutionType.values.contains(entity.institutionType))
        Some(InstitutionType.valueOf(entity.institutionType))
      else None,
      if (entity.id2017 != "") Some(entity.id2017) else None,
      if (entity.taxId != "") Some(entity.taxId) else None,
      if (entity.rssd != "") Some(entity.rssd) else None,
      entity.emailDomains.split(",").toSeq,
      Respondent(
        if (entity.respondentName != "") Some(entity.respondentName) else None,
        if (entity.respondentState != "") Some(entity.respondentState)
        else None,
        if (entity.respondentCity != "") Some(entity.respondentCity) else None
      ),
      Parent(
        if (entity.parentIdRssd != 0) Some(entity.parentIdRssd) else None,
        if (entity.parentName != "") Some(entity.parentName) else None
      ),
      if (entity.assets != 0) Some(entity.assets) else None,
      if (entity.otherLenderCode != 0) Some(entity.otherLenderCode) else None,
      TopHolder(
        if (entity.topHolderIdRssd != 0) Some(entity.topHolderIdRssd) else None,
        if (entity.topHolderName != "") Some(entity.topHolderName) else None
      ),
      entity.hmdaFiler
    )
  }
}
