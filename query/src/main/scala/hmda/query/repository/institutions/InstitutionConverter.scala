package hmda.query.repository.institutions

import java.time.{ Instant, LocalDateTime, ZoneId }

import hmda.model.institution.Institution
import hmda.query.model.institutions.InstitutionQuery
import scala.language.implicitConversions

object InstitutionConverter {

  implicit def toInstitutionQuery(i: Institution): InstitutionQuery = {
    val dateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
    val year = dateTime.getYear
    InstitutionQuery(
      i.id,
      i.agency.value,
      year,
      i.activityYear,
      i.respondent.id.id,
      i.institutionType.entryName,
      i.cra,
      i.emailDomains.email2015,
      i.emailDomains.email2014,
      i.emailDomains.email2013,
      i.respondent.name,
      i.respondent.state,
      i.respondent.city,
      i.respondent.fipsStateNumber,
      i.hmdaFilerFlag,
      i.parent.respondentId,
      i.parent.idRssd,
      i.parent.name,
      i.parent.city,
      i.parent.state,
      i.assets,
      i.otherLenderCode,
      i.topHolder.idRssd,
      i.topHolder.name,
      i.topHolder.city,
      i.topHolder.state,
      i.topHolder.country
    )
  }
}
