package hmda

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.ConfigFactory
import hmda.api.HmdaApi
import hmda.cluster.HmdaClusterRoles
import hmda.persistence.HmdaPersistence
import hmda.query.HmdaQuery
import hmda.validation.HmdaValidation
import org.slf4j.LoggerFactory

object HmdaPlatform extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      |
      | #     # #     # ######     #       ######                                                     #     #  #####
      | #     # ##   ## #     #   # #      #     # #        ##   ##### ######  ####  #####  #    #    #     # #     #
      | #     # # # # # #     #  #   #     #     # #       #  #    #   #      #    # #    # ##  ##    #     #       #
      | ####### #  #  # #     # #     #    ######  #      #    #   #   #####  #    # #    # # ## #    #     #  #####
      | #     # #     # #     # #######    #       #      ######   #   #      #    # #####  #    #     #   #  #
      | #     # #     # #     # #     #    #       #      #    #   #   #      #    # #   #  #    #      # #   #
      | #     # #     # ######  #     #    #       ###### #    #   #   #       ####  #    # #    #       #    #######|
      |
      |
      """.stripMargin
  )

  val config = ConfigFactory.load()

  val clusterRoleConfig = sys.env
    .get("HMDA_CLUSTER_ROLES")
    .map(roles => s"akka.cluster.roles = [$roles]")
    .getOrElse("")

  val runtimeMode = config.getString("hmda.runtime-mode")

  val clusterConfig = if (runtimeMode == "dev") {
    ConfigFactory
      .parseString("""
          |akka {
          |  remote {
          |    netty.tcp {
          |      hostname = 127.0.0.1
          |      port = 2551
          |    }
          |  }
          |  cluster {
          |    seed-nodes = ["akka.tcp://hmda2@127.0.0.1:2551"]
          |  }
          |}
        """.stripMargin)
      .withFallback(config)
  } else {
    ConfigFactory.parseString(clusterRoleConfig).withFallback(config)
  }

  val system =
    ActorSystem(clusterConfig.getString("clustering.name"), clusterConfig)

  val cluster = Cluster(system)

  if (runtimeMode == "prod") {
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
  }

  //Start Persistence
  if (cluster.selfRoles.contains(HmdaClusterRoles.persistence)) {
    system.actorOf(HmdaPersistence.props, HmdaPersistence.name)
  }

  //Start Query
  if (cluster.selfRoles.contains(HmdaClusterRoles.query)) {
    system.actorOf(HmdaQuery.props, HmdaQuery.name)
  }

  //Start Validation
  if (cluster.selfRoles.contains(HmdaClusterRoles.validation)) {
    system.actorOf(HmdaValidation.props, HmdaValidation.name)
  }

  //Start API
  if (cluster.selfRoles.contains(HmdaClusterRoles.api)) {
    system.actorOf(HmdaApi.props, HmdaApi.name)
  }

}
