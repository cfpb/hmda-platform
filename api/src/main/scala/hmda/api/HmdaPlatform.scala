package hmda.api

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.processing.{ LocalHmdaEventProcessor, SingleLarValidation }

import scala.concurrent.ExecutionContext

object HmdaPlatform {

  val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("hmda")
    val supervisor = createSupervisor(system)
    implicit val ec = system.dispatcher

    startActors(system, supervisor)
    startApi(system)

  }

  private def startActors(system: ActorSystem, supervisor: ActorRef)(implicit ec: ExecutionContext): Unit = {
    lazy val actorTimeout = config.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(actorTimeout.seconds)

    (supervisor ? FindActorByName(SingleLarValidation.name))
      .mapTo[ActorRef]
      .map { actor =>
        //log.info(s"Started validator at ${actor.path}")
      }

    (supervisor ? FindActorByName(LocalHmdaEventProcessor.name))
      .mapTo[ActorRef]
      .map { actor =>
        //log.info(s"Started event processor at ${actor.path}")
      }

    (supervisor ? FindActorByName(InstitutionPersistence.name))
      .mapTo[ActorRef]
      .map { actor =>
        //log.info(s"Started institutions at ${actor.path}")
      }

    //Load demo data
    lazy val isDemo = config.getBoolean("hmda.isDemo")
    if (isDemo) {
      DemoData.loadDemoData(system)
    }
  }

  private def startApi(system: ActorSystem): Unit = {
    system.actorOf(HmdaFilingApi.props(), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(), "hmda-admin-api")
  }

}
