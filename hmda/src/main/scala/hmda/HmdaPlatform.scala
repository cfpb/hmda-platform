package hmda

import akka.{actor => untyped}
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.persistence.HmdaPersistence
import hmda.validation.HmdaValidation
import org.slf4j.LoggerFactory
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.Cluster
import hmda.api.HmdaApi
import hmda.persistence.submission.repositories.SyntacticalDb
import hmda.persistence.util.CassandraUtil
import hmda.publication.HmdaPublication
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import com.outworkers.phantom.dsl._
import hmda.census.records.CensusRecords.parseCensusFile
import hmda.model.census.Census

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
      | #     # #     # ######  #     #    #       ###### #    #   #   #       ####  #    # #    #       #    #######
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

  implicit val system =
    untyped.ActorSystem(clusterConfig.getString("hmda.cluster.name"),
                        clusterConfig)

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
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      9092,
      2182,
      Map("offsets.topic.replication.factor" -> "1",
          "zookeeper.connection.timeout.ms" -> "20000")
    )
    EmbeddedKafka.start()
  }

  val appDb = SyntacticalDb(config)
  appDb.create()

  val (indexedTract, indexedCounty, indexedSmallCounty) =
    parseCensusFile.foldLeft(
      (Map[String, Census](), Map[String, Census](), Map[String, Census]())) {
      case ((m1, m2, m3), c) =>
        (
          m1 + (c.toHmdaTract -> c),
          m2 + (c.toHmdaCounty -> c),
          if (c.smallCounty)
            m3 + (c.toHmdaCounty -> c)
          else m3
        )
    }


  //Start Persistence
  system.spawn(HmdaPersistence.behavior, HmdaPersistence.name)

  //Start Validation
  system.spawn(HmdaValidation.behavior, HmdaValidation.name)

  //Start Publication
  system.spawn(HmdaPublication.behavior, HmdaPublication.name)

  //Start API
  system.actorOf(HmdaApi.props, HmdaApi.name)
}
