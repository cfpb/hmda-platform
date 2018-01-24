package hmda

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.http.api.HmdaApi
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
    config
  }

  implicit val system =
    ActorSystem(clusterConfig.getString("hmda.cluster.name"), clusterConfig)

  implicit val mat = ActorMaterializer()
  implicit val cluster = Cluster(system)

  AkkaManagement(system).start()

  if (runtimeMode == "prod") {
    ClusterBootstrap(system).start()
  }

  //Start Persistence
  //system.actorOf(HmdaPersistence.props, HmdaPersistence.name)

  //Start Query
  //system.actorOf(HmdaQuery.props, HmdaQuery.name)

  //Start Validation
  //system.actorOf(HmdaValidation.props, HmdaValidation.name)

  //Start API
  //system.actorOf(HmdaApi.props, HmdaApi.name)
}
