package hmda.api

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import hmda.persistence.HmdaSupervisor._
import hmda.query.HmdaQuerySupervisor._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SingleLarValidation
import hmda.query.view.institutions.InstitutionView
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object HmdaPlatform {

  val config = ConfigFactory.load()

  val log = LoggerFactory.getLogger("hmda")

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("hmda")
    val supervisor = createSupervisor(system)
    val querySupervisor = createQuerySupervisor(system)
    implicit val ec = system.dispatcher

    startActors(system, supervisor, querySupervisor)
    startApi(system)

  }

  private def startActors(system: ActorSystem, supervisor: ActorRef, querySupervisor: ActorRef)(implicit ec: ExecutionContext): Unit = {
    lazy val actorTimeout = config.getInt("hmda.actor.timeout")
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

    //Load demo data
    lazy val isDemo = config.getBoolean("hmda.isDemo")
    if (isDemo) {
      DemoData.loadDemoData(system)
    }

    // Start query Actors
    (querySupervisor ? FindActorByName(InstitutionView.name))
      .mapTo[ActorRef]

  }

  private def startApi(system: ActorSystem): Unit = {
    system.actorOf(HmdaFilingApi.props(), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(), "hmda-admin-api")
  }

}
