package hmda.cluster

import java.io.File

import org.slf4j.LoggerFactory
import akka.actor._
import akka.pattern.ask
import akka.cluster.Cluster
import akka.cluster.http.management.ClusterHttpManagement
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.persistence.HmdaSupervisor
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.publication.HmdaPublication
import hmda.query.{ HmdaProjectionQuery, HmdaQuerySupervisor }
import hmda.query.view.institutions.InstitutionView
import hmda.validation.ValidationStats
import hmda.query.DbConfiguration._
import hmda.query.projections.institutions.InstitutionDBProjection.{ CreateSchema, _ }
import hmda.cluster.HmdaConfig._
import hmda.persistence.demo.DemoData
import hmda.persistence.messages.events.institutions.InstitutionEvents.InstitutionSchemaCreated

import scala.concurrent.duration._
import hmda.query.projections.filing.HmdaFilingDBProjection._
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef
import hmda.util.FutureRetry.retry

object HmdaPlatform extends App {

  val log = LoggerFactory.getLogger("hmda")
  val clusterRoleConfig = sys.env.get("HMDA_CLUSTER_ROLES").map(roles => s"akka.cluster.roles = [$roles]").getOrElse("")
  val clusterConfig = ConfigFactory.parseString(clusterRoleConfig).withFallback(configuration)
  val system = ActorSystem(clusterConfig.getString("clustering.name"), clusterConfig)
  val cluster = Cluster(system)

  val actorTimeout = clusterConfig.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(actorTimeout.seconds)

  //Start API
  if (cluster.selfRoles.contains("api")) {
    ClusterHttpManagement(cluster).start()
    system.actorOf(HmdaFilingApi.props().withDispatcher("api-dispatcher"), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props().withDispatcher("api-dispatcher"), "hmda-admin-api")
    system.actorOf(HmdaPublicApi.props().withDispatcher("api-dispatcher"), "hmda-public-api")
  }

  //Start Persistence
  if (cluster.selfRoles.contains("persistence")) {
    val supervisor = system.actorOf(HmdaSupervisor.props().withDispatcher("persistence-dispatcher"), "supervisor")
    implicit val ec = system.dispatchers.lookup("persistence-dispatcher")
    (supervisor ? FindActorByName(SingleLarValidation.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started single lar validator at ${a.path}"))
    (supervisor ? FindActorByName(InstitutionPersistence.name))
      .mapTo[ActorRef]
      .map(a => log.info(s"Started institutions at ${a.path}"))
  }

  //Start Query
  if (cluster.selfRoles.contains("query")) {
    val querySupervisor = system.actorOf(
      Props[HmdaQuerySupervisor].withDispatcher("query-dispatcher"),
      "query-supervisor"
    )
    implicit val ec = system.dispatchers.lookup("query-dispatcher")
    val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    institutionViewF.map(actorRef => loadDemoData(actorRef))
    HmdaProjectionQuery.startUp(system)
  }

  //Start Publication
  if (cluster.selfRoles.contains("publication")) {
    system.actorOf(Props[HmdaPublication].withDispatcher("publication-dispatcher"), "publication")
  }

  //Start Validation
  if (cluster.selfRoles.contains("validation")) {
    system.actorOf(ValidationStats.props().withDispatcher("validation-dispatcher"), "validation-stats")
  }

  //Load demo data
  def loadDemoData(institutionView: ActorRef): Unit = {
    val isDemo = clusterConfig.getBoolean("hmda.isDemo")
    if (isDemo) {
      implicit val ec = system.dispatcher
      cleanup()
      implicit val scheduler = system.scheduler
      val retries = List(200.millis, 200.millis, 500.millis, 1.seconds, 2.seconds)
      log.info("...LOADING DEMO DATA...")

      val institutionCreatedF = for {
        q <- retry((institutionView ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
        s <- (q ? CreateSchema).mapTo[InstitutionSchemaCreated]
      } yield s

      institutionCreatedF.map { x =>
        log.info(x.toString)
        DemoData.loadDemoData(system)
      }
    }
  }

  private def cleanup(): Unit = {
    // Delete persistence journal
    val file = new File("target/journal")
    if (file.isDirectory) {
      log.info("CLEANING JOURNAL")
      file.listFiles.foreach(f => f.delete())
    }

    val larRepository = new LarRepository(config)
    val institutionRepository = new InstitutionRepository(config)

    larRepository.dropSchema()
    institutionRepository.dropSchema()
  }

}
