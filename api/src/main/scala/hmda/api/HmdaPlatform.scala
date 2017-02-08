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
import hmda.query.projections.institutions.InstitutionDBProjection.{ CreateSchema, DeleteSchema }
import hmda.query.view.institutions.InstitutionView
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionSchemaCreated, InstitutionSchemaDeleted }
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef
import org.slf4j.LoggerFactory
import hmda.future.util.FutureRetry._
import hmda.query.projections.filing.HmdaFilingDBProjection.FilingSchemaDeleted

import scala.concurrent.{ ExecutionContext, Future }

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

  private def cleanup(system: ActorSystem, supervisor: ActorRef, querySupervisor: ActorRef)(implicit ec: ExecutionContext): Future[InstitutionSchemaDeleted] = {
    lazy val actorTimeout = config.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(actorTimeout.seconds)
    implicit val scheduler = system.scheduler
    val retries = List(200.millis, 200.millis, 500.millis, 1.seconds, 2.seconds)

    val hmdaFilingViewF = (supervisor ? FindHmdaFiling("2017")).mapTo[ActorRef]
    val hmdaTableDropF = for {
      h <- hmdaFilingViewF
      r <- retry((h ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
      d <- (r ? DeleteSchema).mapTo[FilingSchemaDeleted]
    } yield d

    hmdaTableDropF.map { x =>
      log.info("CLEANING.... " + x.toString)
    }

    val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
      .mapTo[ActorRef]
    for {
      i <- institutionViewF
      q <- retry((i ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
      s <- (q ? DeleteSchema).mapTo[InstitutionSchemaDeleted]
    } yield s
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

    // Start query Actors
    val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
      .mapTo[ActorRef]

    //Load demo data
    lazy val isDemo = config.getBoolean("hmda.isDemo")
    if (isDemo) {
      val institutionTableDropF = cleanup(system, supervisor, querySupervisor)
      implicit val scheduler = system.scheduler
      val retries = List(200.millis, 200.millis, 500.millis, 1.seconds, 2.seconds)
      log.info("...LOADING DEMO DATA...")
      institutionTableDropF.map { x =>
        val institutionCreatedF = for {
          i <- institutionViewF
          q <- retry((i ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
          s <- (q ? CreateSchema).mapTo[InstitutionSchemaCreated]
        } yield s

        institutionCreatedF.map { x =>
          log.info(x.toString)
          DemoData.loadDemoData(system)
        }
      }
    }

  }

  private def startApi(system: ActorSystem): Unit = {
    system.actorOf(HmdaFilingApi.props(), "hmda-filing-api")
    system.actorOf(HmdaAdminApi.props(), "hmda-admin-api")
  }

}
