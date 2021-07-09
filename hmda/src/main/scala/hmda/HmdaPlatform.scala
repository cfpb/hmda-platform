package hmda

import java.net.InetAddress

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.cluster.typed.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import hmda.api.HmdaApi
import hmda.persistence.HmdaPersistence
import hmda.persistence.util.CassandraUtil
import hmda.publication.{HmdaPublication, KafkaUtils}
import hmda.validation.HmdaValidation
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.slf4j.LoggerFactory

// $COVERAGE-OFF$
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
    case "dev" =>
      ConfigFactory.parseResources("application-dev.conf").resolve()
    case "docker-compose" =>
      ConfigFactory.parseResources("application-dev.conf").resolve()
    case "dev-node" =>
      ConfigFactory.parseResources("application-dev.conf").resolve()

    case "kind" =>
      ConfigFactory.parseResources("application-kind.conf").resolve()

    case "kubernetes" =>
      log.info(s"HOSTNAME: ${System.getenv("HOSTNAME")}")
      log.info(s"HOSTADDRESS: " + InetAddress.getLocalHost().getHostAddress())
      ConfigFactory.parseResources("application-kubernetes.conf").resolve()

    case "dcos" =>
      ConfigFactory.parseResources("application-dcos.conf").resolve()
    case _ =>
      config
  }

  implicit val classic: ClassicActorSystem = ClassicActorSystem(clusterConfig.getString("hmda.cluster.name"), clusterConfig)
  implicit val system: ActorSystem[_]      = classic.toTyped

  implicit val mat     = Materializer(system)
  implicit val cluster = Cluster(system)

  if (runtimeMode == "dcos" || runtimeMode == "kubernetes" || runtimeMode == "docker-compose" || runtimeMode == "kind") {
    ClusterBootstrap(system).start()
    AkkaManagement(system).start()
  }

  if (runtimeMode == "dev") {
    CassandraUtil.startEmbeddedCassandra()
    AkkaManagement(system).start()
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      sys.env.getOrElse("HMDA_LOCAL_KAFKA_PORT", "9092").toInt,
      sys.env.getOrElse("HMDA_LOCAL_ZK_PORT", "2182").toInt,
      Map("offsets.topic.replication.factor" -> "1", "zookeeper.connection.timeout.ms" -> "20000")
    )
    EmbeddedKafka.start()
  }

  // TODO: Fix this as initializing it here is not a good idea, this should be initialized in HmdaPersistence and passed into HmdaValidationError
  val stringKafkaProducer      = KafkaUtils.getStringKafkaProducer(system)
  val institutionKafkaProducer = KafkaUtils.getInstitutionKafkaProducer(system)

  //Start Persistence
  classic.spawn(HmdaPersistence(), HmdaPersistence.name)

  //Start Validation
  classic.spawn(HmdaValidation(), HmdaValidation.name)

  //Start Publication
  classic.spawn(HmdaPublication(), HmdaPublication.name)

  //Start API
  classic.spawn[Nothing](HmdaApi(), HmdaApi.name)
}
// $COVERAGE-ON$
