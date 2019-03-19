package hmda.cluster

import java.io.File

import org.slf4j.LoggerFactory
import akka.actor._
import akka.pattern.ask
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings }
import akka.util.Timeout
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.api.tcp.admin.InstitutionAdminTcpApi
import hmda.api.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.persistence.HmdaSupervisor
import hmda.persistence.institutions.{ HmdaFilerPersistence, InstitutionPersistence }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.query.{ HmdaProjectionQuery, HmdaQuerySupervisor }
import hmda.cluster.HmdaConfig._
import hmda.persistence.HmdaSupervisor.{ FindAPORPersistence, FindHmdaFilerPersistence }
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence.LoadAporDataFromS3
import hmda.persistence.demo.DemoData
import hmda.persistence.messages.CommonMessages._
import hmda.publication.HmdaPublicationSupervisor
import hmda.publication.regulator.lar.{ ModifiedLarPublisher, RegulatorLarPublisher }
import hmda.publication.regulator.panel.RegulatorPanelPublisher
import hmda.publication.regulator.ts.RegulatorTsPublisher
import hmda.query.HmdaQuerySupervisor.{ FindSignedEventLARSubscriber, FindSignedEventTSSubscriber }
import hmda.validation.stats.ValidationStats

import scala.concurrent.duration._

object HmdaPlatform extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | #     # #     # ######     #       ######
      | #     # ##   ## #     #   # #      #     # #        ##   ##### ######  ####  #####  #    #
      | #     # # # # # #     #  #   #     #     # #       #  #    #   #      #    # #    # ##  ##
      | ####### #  #  # #     # #     #    ######  #      #    #   #   #####  #    # #    # # ## #
      | #     # #     # #     # #######    #       #      ######   #   #      #    # #####  #    #
      | #     # #     # #     # #     #    #       #      #    #   #   #      #    # #   #  #    #
      | #     # #     # ######  #     #    #       ###### #    #   #   #       ####  #    # #    #
      |
      """.stripMargin
  )

  val clusterRoleConfig = sys.env.get("HMDA_CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val clusterConfig = ConfigFactory.parseString(clusterRoleConfig).withFallback(configuration)
  val system = ActorSystem(clusterConfig.getString("clustering.name"), clusterConfig)
  val cluster = Cluster(system)

  val actorTimeout = clusterConfig.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(actorTimeout.seconds)

  val supervisorProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/${HmdaSupervisor.name}",
      settings = ClusterSingletonProxySettings(system).withRole("persistence")
    ),
    name = "supervisorProxy"
  )

  val querySupervisorProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/${HmdaQuerySupervisor.name}",
      settings = ClusterSingletonProxySettings(system).withRole("query")
    ),
    name = "querySupervisorProxy"
  )

  val publicationSupervisorProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/${HmdaPublicationSupervisor.name}",
      settings = ClusterSingletonProxySettings(system).withRole("publication")
    ),
    name = "publicationSupervisorProxy"
  )

  val validationStatsProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/${ValidationStats.name}",
      settings = ClusterSingletonProxySettings(system).withRole("persistence")
    )
  )

  //Start API
  if (cluster.selfRoles.contains("api")) {
    ClusterHttpManagement(cluster).start()
    system.actorOf(HmdaFilingApi.props(supervisorProxy, querySupervisorProxy, validationStatsProxy).withDispatcher("api-dispatcher"), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(supervisorProxy, publicationSupervisorProxy).withDispatcher("api-dispatcher"), "hmda-admin-api")
    system.actorOf(HmdaPublicApi.props(supervisorProxy).withDispatcher("api-dispatcher"), "hmda-public-api")
    system.actorOf(InstitutionAdminTcpApi.props(supervisorProxy), "panel-loader-tcp")
  }

  //Start Persistence
  if (cluster.selfRoles.contains("persistence")) {
    implicit val ec = system.dispatchers.lookup("persistence-dispatcher")

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = ValidationStats.props(),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("persistence")
      ),
      name = ValidationStats.name
    )

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = HmdaSupervisor.props(validationStatsProxy),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("persistence")
      ),
      name = HmdaSupervisor.name
    )

    (supervisorProxy ? FindActorByName(SingleLarValidation.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started single lar validator at ${a.path}"))

    (supervisorProxy ? FindActorByName(InstitutionPersistence.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started institutions at ${a.path}"))

    (supervisorProxy ? FindAPORPersistence(HmdaAPORPersistence.name))
      .mapTo[ActorRef]
      .map { a =>
        QuartzSchedulerExtension(system).schedule("AporCalculator", a, LoadAporDataFromS3)
        log.info(s"Started Rate Spread calculator at ${a.path}")
      }

    (supervisorProxy ? FindHmdaFilerPersistence(HmdaFilerPersistence.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started filers at ${a.path}"))
  }

  //Start Query
  if (cluster.selfRoles.contains("query")) {
    implicit val ec = system.dispatchers.lookup("query-dispatcher")

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = Props(classOf[HmdaQuerySupervisor]),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("query")
      ),
      name = HmdaQuerySupervisor.name
    )

    loadDemoData(supervisorProxy)

    HmdaProjectionQuery.startUp(system)

    (querySupervisorProxy ? FindSignedEventLARSubscriber)
      .mapTo[ActorRef]
      .map(a => log.info(s"Started submission signed event LAR subscriber at ${a.path}"))

    (querySupervisorProxy ? FindSignedEventTSSubscriber)
      .mapTo[ActorRef]
      .map(a => log.info(s"Started submission signed event TS subscriber at ${a.path}"))

  }

  //Start Publication
  if (cluster.selfRoles.contains("publication")) {
    system.actorOf(ModifiedLarPublisher.props(supervisorProxy).withDispatcher("publication-dispatcher"), "modified-lar-publisher")
    system.actorOf(RegulatorTsPublisher.props().withDispatcher("publication-dispatcher"), "regulator-ts-publisher")
    system.actorOf(RegulatorLarPublisher.props(supervisorProxy).withDispatcher("publication-dispatcher"), "regulator-lar-publisher")
    system.actorOf(RegulatorPanelPublisher.props().withDispatcher("publication-dispatcher"), "regulator-panel-publisher")

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = Props(classOf[HmdaPublicationSupervisor]),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("publication")
      ),
      name = HmdaPublicationSupervisor.name
    )

  }

  //Load demo data
  def loadDemoData(supervisor: ActorRef): Unit = {
    val isDemo = clusterConfig.getBoolean("hmda.isDemo")
    if (isDemo) {
      implicit val ec = system.dispatcher
      cleanup()
      log.info("*** LOADING DEMO DATA ***")
      val institutionCreatedF = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
      institutionCreatedF.map(i => DemoData.loadDemoData(system, i))
    }
  }

  private def cleanup(): Unit = {
    // Delete persistence journal
    val file = new File("target/journal")
    if (file.isDirectory) {
      log.info("CLEANING JOURNAL")
      file.listFiles.foreach(f => f.delete())
    }
  }

}
