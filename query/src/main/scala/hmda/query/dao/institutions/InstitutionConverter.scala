package hmda.query.dao.institutions

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
      i.name,
      i.cra,
      i.agency.value,
      i.institutionType.entryName,
      i.hasParent,
      i.status.code,
      year
    )
  }
}
