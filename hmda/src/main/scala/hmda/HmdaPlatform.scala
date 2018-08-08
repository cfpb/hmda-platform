package hmda

import akka.{actor => untyped}
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.HmdaApi
import hmda.persistence.HmdaPersistence
import hmda.query.HmdaQuery
import hmda.validation.HmdaValidation
import org.slf4j.LoggerFactory
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.Cluster
import hmda.persistence.util.CassandraUtil
import hmda.publication.HmdaPublication

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
    case "dev-node" =>
      ConfigFactory.parseResources("application-dev.conf").resolve()
    case "kubernetes" => {
      log.info(s"HOSTNAME: ${System.getenv("HOSTNAME")}")
      ConfigFactory.parseResources("application-kubernetes.conf").resolve()
    }
    case "dcos" =>
      ConfigFactory.parseResources("application-dcos.conf").resolve()
    case _ => config
  }

  val mergedConfig = clusterConfig.withFallback(config)

  log.info(s"Config: $config.root().render()")
  log.info(s"Cluster Config: $clusterConfig.root().render()")
  log.info(s"Merged Config: $mergedConfig.root().render()")

  implicit val system =
    untyped.ActorSystem(mergedConfig.getString("hmda.cluster.name"),
      mergedConfig)

  implicit val typedSystem = system.toTyped

  implicit val mat = ActorMaterializer()
  implicit val cluster = Cluster(typedSystem)

  if (runtimeMode == "dcos" || runtimeMode == "kubernetes") {
    ClusterBootstrap(system).start()
    AkkaManagement(system).start()
  }

  if (runtimeMode == "dev") {
    CassandraUtil.startEmbeddedCassandra()
    AkkaManagement(system).start()
  }

  //Start Persistence
  system.spawn(HmdaPersistence.behavior, HmdaPersistence.name)

  //Start Query
  system.spawn(HmdaQuery.behavior, HmdaQuery.name)

  //Start Validation
  system.spawn(HmdaValidation.behavior, HmdaValidation.name)

  //Start Publication
  system.spawn(HmdaPublication.behavior, HmdaPublication.name)

  //Start API
  system.actorOf(HmdaApi.props, HmdaApi.name)
}
