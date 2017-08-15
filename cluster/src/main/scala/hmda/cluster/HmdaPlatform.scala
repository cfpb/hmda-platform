package hmda.cluster

import org.slf4j.LoggerFactory
import hmda.cluster.HmdaConfig._
import akka.actor._
import akka.pattern.ask
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.publication.HmdaPublication
import hmda.query.HmdaQuerySupervisor
import hmda.query.view.institutions.InstitutionView
import hmda.validation.ValidationStats

import scala.concurrent.duration._

object HmdaPlatform extends App {

  val log = LoggerFactory.getLogger("hmda")
  val clusterRoleConfig = sys.env.get("HMDA_CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val config = ConfigFactory.parseString(clusterRoleConfig).withFallback(configuration)
  val system = ActorSystem(configuration.getString("clustering.name"), config)
  val cluster = Cluster(system)

  val actorTimeout = config.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(actorTimeout.seconds)

  //Start API
  if (cluster.selfRoles.contains("api")) {
    ClusterHttpManagement(cluster).start()
    system.actorOf(HmdaFilingApi.props(), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(), "hmda-admin-api")
    system.actorOf(HmdaPublicApi.props(), "hmda-public-api")
  }

  //Start Query
  if (cluster.selfRoles.contains("query")) {
    val querySupervisor = system.actorOf(Props[HmdaQuerySupervisor], "query-supervisor")
    val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
      .mapTo[ActorRef]
  }

  //Start Publication
  if (cluster.selfRoles.contains("publication")) {
    system.actorOf(Props[HmdaPublication].withDispatcher("publication-dispatcher"), "publication")
  }

  //Start Validation
  if (cluster.selfRoles.contains("validation")) {
    system.actorOf(ValidationStats.props().withDispatcher("validation-dispatcher"), "validation-stats")
  }

}
