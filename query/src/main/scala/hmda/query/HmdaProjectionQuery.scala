package hmda.query

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.query.projections.filing.FilingCassandraProjection
import hmda.query.projections.institutions.InstitutionCassandraProjection

object HmdaProjectionQuery {

  def startUp(implicit system: ActorSystem): Unit = {
    implicit val materializer = ActorMaterializer()
    implicit val ec = system.dispatcher
    val institutionProjection = new InstitutionCassandraProjection(system, materializer)
    institutionProjection.startUp()

    val filingProjection = new FilingCassandraProjection(system, materializer)
    filingProjection.startUp()
  }

}
