package hmda.query

import hmda.query.projections.filing.FilingCassandraProjection
import hmda.query.projections.institutions.InstitutionCassandraProjection

object HmdaProjectionQuery extends App {

  val institutionProjection = new InstitutionCassandraProjection
  institutionProjection.startUp()

  val filingProjection = new FilingCassandraProjection
  filingProjection.startUp()

}
