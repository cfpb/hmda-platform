package hmda.api

import java.io.File

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import hmda.persistence.HmdaSupervisor._
import hmda.query.HmdaQuerySupervisor._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef
import org.slf4j.LoggerFactory
import hmda.util.FutureRetry._
import hmda.validation.ValidationStats
import hmda.api.HmdaConfig._
import hmda.query.HmdaProjectionQuery

object HmdaPlatform {

  val log = LoggerFactory.getLogger("hmda")

  def main(args: Array[String]): Unit = {

    val system = ActorSystem(configuration.getString("clustering.name"), configuration)
    val supervisor = createSupervisor(system)
    val querySupervisor = createQuerySupervisor(system)
    implicit val ec = system.dispatcher

    startActors(system, supervisor, querySupervisor)
    startApi(system, querySupervisor)

  }

  private def cleanup(): Unit = {
    // Delete persistence journal
    val file = new File("target/journal")
    if (file.isDirectory) {
      log.info("CLEANING JOURNAL")
      file.listFiles.foreach(f => f.delete())
    }
  }

  private def startActors[_: EC](system: ActorSystem, supervisor: ActorRef, querySupervisor: ActorRef): Unit = {
    lazy val actorTimeout = configuration.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(actorTimeout.seconds)

    (supervisor ? FindActorByName(SingleLarValidation.name))
      .mapTo[ActorRef]
      .map { actor =>
        log.info(s"Started validator at ${actor.path}")
      }

    (supervisor ? FindActorByName(InstitutionPersistence.name))
      .mapTo[ActorRef]
      .map { actor =>
        log.info(s"Started institutions at ${actor.path}")
      }

    // Start query Actors
    val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
      .mapTo[ActorRef]

    //Start Query Projections
    HmdaProjectionQuery.main(Array.empty[String])

    // Start validation stats actor
    system.actorOf(ValidationStats.props(), "validation-stats")

    //Load demo data
    lazy val isDemo = configuration.getBoolean("hmda.isDemo")
    if (isDemo) {
      cleanup()
      implicit val scheduler = system.scheduler
      val retries = List(200.millis, 200.millis, 500.millis, 1.seconds, 2.seconds)
      log.info("...LOADING DEMO DATA...")

      val institutionCreatedF = for {
        i <- institutionViewF
        q <- retry((i ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
      } yield q

      institutionCreatedF.map { x =>
        log.info(x.toString)
        DemoData.loadDemoData(system)
      }
    }

  }

  private def startApi(system: ActorSystem, querySupervisor: ActorRef): Unit = {
    system.actorOf(HmdaFilingApi.props(), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(), "hmda-admin-api")
    system.actorOf(HmdaPublicApi.props(querySupervisor), "hmda-public-api")
  }

}
