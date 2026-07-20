package hmda.persistence.migrator

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.cassandra.reconciler.Reconciliation
import org.slf4j.LoggerFactory

import scala.util.{ Failure, Success }

object HmdaPersistenceReconciler {
  private val log = LoggerFactory.getLogger(getClass)
  val name = "HmdaPersistenceReconciler"


  val main: Behavior[Any] = Behaviors.setup { context =>
    implicit val system: ActorSystem[Nothing] = context.system

    log.info("starting reconcile")

    val reconciler = new Reconciliation(system)

    context.pipeToSelf(reconciler.rebuildAllPersistenceIds())(_: Any => _)

    Behaviors.receiveMessage {
      case Success(_) =>
        log.info("finished reconciliation.")
        Behaviors.stopped
      case Failure(e) =>
        log.error("Failure encountered.", e)
        Behaviors.stopped
    }
  }
}
