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
      i.respondentId.id,
      i.institutionType.entryName,
      i.cra,
      i.emailDomain2015,
      i.emailDomain2014,
      i.emailDomain2013,
      i.respondentName,
      i.respondentState,
      i.respondentCity,
      i.respondentFipsStateNumber,
      i.hmdaFilerFlag,
      i.parentRespondentId,
      i.parentIdRssd,
      i.parentName,
      i.parentCity,
      i.parentState,
      i.assets,
      i.otherLenderCode,
      i.topHolderIdRssd,
      i.topHolderName,
      i.topHolderCity,
      i.topHolderState,
      i.topHolderCountry
    )
  }
}
