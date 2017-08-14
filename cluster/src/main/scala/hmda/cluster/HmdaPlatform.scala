package hmda.cluster

import org.slf4j.LoggerFactory
import hmda.cluster.HmdaConfig._
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import com.typesafe.config.ConfigFactory
import hmda.publication.HmdaPublication
import hmda.query.HmdaQuerySupervisor

object HmdaPlatform extends App {

  val log = LoggerFactory.getLogger("hmda")
  val clusterRoleConfig = sys.env.get("HMDA_CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val config = ConfigFactory.parseString(clusterRoleConfig).withFallback(configuration)
  val system = ActorSystem(configuration.getString("clustering.name"), config)
  val cluster = Cluster(system)

  //Start API
  if (cluster.selfRoles.contains("api")) {
    ClusterHttpManagement(cluster).start()
  }

  //Start Query
  if (cluster.selfRoles.contains("query")) {
    system.actorOf(Props[HmdaQuerySupervisor], "query-supervisor")
  }

  //Start Publication
  if (cluster.selfRoles.contains("publication")) {
    system.actorOf(Props[HmdaPublication], "publication")
  }

}
