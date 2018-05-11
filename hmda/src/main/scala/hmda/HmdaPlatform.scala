package hmda

import akka.{actor => untyped}
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
import akka.actor.typed.scaladsl.adapter._
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

  val clusterConfig = if (runtimeMode == "dev") {
    ConfigFactory.parseResources("application-dev.conf").resolve()
  } else {
    config
  }

  implicit val system =
    untyped.ActorSystem(clusterConfig.getString("hmda.cluster.name"),
                        clusterConfig)

  implicit val mat = ActorMaterializer()
  implicit val cluster = Cluster(system)

  if (runtimeMode == "prod") {
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()
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
