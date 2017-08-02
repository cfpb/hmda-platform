package hmda.query

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.query.projections.filing.FilingCassandraProjection
import hmda.query.projections.institutions.InstitutionCassandraProjection

object HmdaProjectionQuery extends App {

  val config = ConfigFactory.load()

  implicit val system = ActorSystem(config.getString("clustering.name"))
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val institutionProjection = new InstitutionCassandraProjection(system, materializer)
  institutionProjection.startUp()

  val filingProjection = new FilingCassandraProjection(system, materializer)
  filingProjection.startUp()

}
