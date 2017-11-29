package hmda.cluster

import java.io.File

import org.slf4j.LoggerFactory
import akka.actor._
import akka.pattern.ask
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.tcp.admin.InstitutionAdminTcpApi
import hmda.api.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.persistence.HmdaSupervisor
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.query.{ HmdaProjectionQuery, HmdaQuerySupervisor }
import hmda.query.view.institutions.InstitutionView
import hmda.validation.ValidationStats
import hmda.cluster.HmdaConfig._
import hmda.persistence.demo.DemoData
import hmda.persistence.messages.CommonMessages._
import hmda.publication.submission.lar.SubmissionSignedModifiedLarSubscriber
import hmda.query.HmdaQuerySupervisor.FindSignedEventQuerySubscriber

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
      singletonManagerPath = "/user/supervisor",
      settings = ClusterSingletonProxySettings(system).withRole("persistence")
    ),
    name = "supervisorProxy"
  )

  val querySupervisorProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = "/user/query-supervisor",
      settings = ClusterSingletonProxySettings(system).withRole("query")
    ),
    name = "querySupervisorProxy"
  )

  val validationStatsProxy = system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = "/user/validation-stats",
      settings = ClusterSingletonProxySettings(system).withRole("persistence")
    )
  )

  //Start API
  if (cluster.selfRoles.contains("api")) {
    ClusterHttpManagement(cluster).start()
    system.actorOf(HmdaFilingApi.props(supervisorProxy, querySupervisorProxy, validationStatsProxy).withDispatcher("api-dispatcher"), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(supervisorProxy, querySupervisorProxy).withDispatcher("api-dispatcher"), "hmda-admin-api")
    system.actorOf(HmdaPublicApi.props(querySupervisorProxy).withDispatcher("api-dispatcher"), "hmda-public-api")
    system.actorOf(InstitutionAdminTcpApi.props(supervisorProxy), "panel-loader-tcp")
  }

  //Start Persistence
  if (cluster.selfRoles.contains("persistence")) {
    implicit val ec = system.dispatchers.lookup("persistence-dispatcher")

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = Props(classOf[ValidationStats]),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("persistence")
      ),
      name = "validation-stats"
    )

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = HmdaSupervisor.props(validationStatsProxy),
        terminationMessage = Shutdown,
        settings = ClusterSingletonManagerSettings(system).withRole("persistence")
      ),
      name = "supervisor"
    )

    (supervisorProxy ? FindActorByName(SingleLarValidation.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started single lar validator at ${a.path}"))

    (supervisorProxy ? FindActorByName(InstitutionPersistence.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started institutions at ${a.path}"))
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
      name = "query-supervisor"
    )

    val institutionViewF = (querySupervisorProxy ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    institutionViewF.map(actorRef => loadDemoData(supervisorProxy, actorRef))

    HmdaProjectionQuery.startUp(system)

    (querySupervisorProxy ? FindSignedEventQuerySubscriber)
      .mapTo[ActorRef]
      .map(a => log.info(s"Started submission signed event subscriber at ${a.path}"))

  }

  //Start Publication
  if (cluster.selfRoles.contains("publication")) {
    system.actorOf(SubmissionSignedModifiedLarSubscriber.props(supervisorProxy).withDispatcher("publication-dispatcher"), "modified-lar-subscriber")
    //system.actorOf(Props[HmdaReportsPublication].withDispatcher("publication-dispatcher"), "publication")
  }

  //Load demo data
  def loadDemoData(supervisor: ActorRef, institutionView: ActorRef): Unit = {
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
