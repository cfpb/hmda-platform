package hmda

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.HmdaApi
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

  val runtimeMode = config.getString("hmda.runtime.mode")

  log.info(s"HMDA_RUNTIME_MODE: $runtimeMode")

  val clusterConfig = runtimeMode match {
    case "dev" => ConfigFactory.parseResources("application-dev.conf").resolve()
    case "kubernetes" => {
      log.info(s"HOSTNAME: ${System.getenv("HOSTNAME")}")
      ConfigFactory.parseResources("application-kubernetes.conf").resolve()
    }
    case "dcos" =>
      ConfigFactory.parseResources("application-dcos.conf").resolve()
    case _ => config
  }

  implicit val system =
    ActorSystem(clusterConfig.getString("hmda.cluster.name"), clusterConfig)

  implicit val mat = ActorMaterializer()
  implicit val cluster = Cluster(system)

  AkkaManagement(system).start()

  if (runtimeMode == "dcos" || runtimeMode == "kubernetes") {
    ClusterBootstrap(system).start()
  }

  //Start Persistence
  system.actorOf(HmdaPersistence.props, HmdaPersistence.name)

  //Start Query
  system.actorOf(HmdaQuery.props, HmdaQuery.name)

  //Start Validation
  system.actorOf(HmdaValidation.props, HmdaValidation.name)

  //Start API
  system.actorOf(HmdaApi.props, HmdaApi.name)
}
