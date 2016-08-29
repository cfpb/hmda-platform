package hmda.persistence.institutions

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.model.fi.{ Filing, NotStarted }
import hmda.persistence.CommonMessages._
import hmda.persistence.institutions.FilingPersistence._

object FilingPersistence {

  case class CreateFiling(filing: Filing) extends Command
  case class UpdateFilingStatus(filing: Filing) extends Command
  case class GetFilingByPeriod(period: String) extends Command

  case class FilingCreated(filing: Filing) extends Event
  case class FilingStatusUpdated(filing: Filing) extends Event

  def props(institutionId: String): Props = Props(new FilingPersistence(institutionId))

  def createFilings(institutionId: String, system: ActorSystem): ActorRef = {
    system.actorOf(FilingPersistence.props(institutionId))
  }

  case class FilingState(filings: Seq[Filing] = Nil) {
    def updated(event: Event): FilingState = {
      event match {
        case FilingCreated(f) =>
          FilingState(f +: filings)
        case FilingStatusUpdated(modified) =>
          val x = filings.find(x => x.period == modified.period).getOrElse(Filing())
          val i = filings.indexOf(x)
          FilingState(filings.updated(i, modified))
      }
    }
  }
}

// Filings per Institution (institutionId = institution identifier)
class FilingPersistence(institutionId: String) extends PersistentActor with ActorLogging {

  var state = FilingState()

  def updateState(e: Event): Unit = {
    state = state.updated(e)
  }

  override def preStart(): Unit = {
    log.debug(s"Filings started at ${self.path}")
  }

  override def persistenceId: String = s"filings-$institutionId"

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
    case SnapshotOffer(_, snapshot: FilingState) =>
      log.debug(s"Recovering from snapshot")
      state = snapshot

  }

  override def receiveCommand: Receive = {
    case CreateFiling(f) =>
      if (!state.filings.contains(f)) {
        persist(FilingCreated(f)) { e =>
          log.debug(s"Persisted: $f")
          updateState(e)
        }
      } else {
        log.warning(s"Filing already exists. Could not create $f")
      }

    case UpdateFilingStatus(modified) =>
      if (state.filings.map(x => x.period).contains(modified.period)) {
        persist(FilingStatusUpdated(modified)) { e =>
          log.debug(s"persisted: $modified")
          updateState(e)
        }
      } else {
        log.warning(s"Period does not exist. Could not update $modified")
      }

    case GetFilingByPeriod(period) =>
      val filing = state.filings.find(f => f.period == period).getOrElse(Filing("", institutionId, NotStarted))
      if (state.filings.size == 0)
        sender() ! Filing()
      else
        sender() ! filing

    case GetState =>
      sender() ! state.filings

    case Shutdown =>
      context stop self

  }
}
