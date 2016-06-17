package hmda.api.persistence

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence._
import hmda.model.fi.Filing

object FilingPersistence {

  case class CreateFiling(filing: Filing) extends Command
  case class UpdateFilingStatus(filing: Filing) extends Command
  case class GetFilingByPeriod(period: String) extends Command

  case class FilingCreated(filing: Filing) extends Event
  case class FilingStatusUpdated(filing: Filing) extends Event

  def props(fid: String): Props = Props(new FilingPersistence(fid))

  def createFilings(fid: String, system: ActorSystem): ActorRef = {
    system.actorOf(FilingPersistence.props(fid))
  }

  case class FilingState(filings: Seq[Filing] = Nil) {
    def updated(event: Event): FilingState = {
      event match {
        case FilingCreated(f) =>
          FilingState(f +: filings)
        case FilingStatusUpdated(modified) =>
          val x = filings.find(x => x.id == modified.id).getOrElse(Filing())
          val i = filings.indexOf(x)
          FilingState(filings.updated(i, modified))
      }
    }
  }
}

// Filings per Institution (fid = institution identifier)
class FilingPersistence(fid: String) extends PersistentActor with ActorLogging {

  var state = FilingState()

  def updateState(e: Event): Unit = {
    state = state.updated(e)
  }

  override def preStart(): Unit = {
    log.info(s"Filings started at ${self.path}")
  }

  override def persistenceId: String = s"filings-$fid"

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
    case SnapshotOffer(_, snapshot: FilingState) =>
      log.info(s"Recovering from snapshot")
      state = snapshot

  }

  override def receiveCommand: Receive = {
    case CreateFiling(f) =>
      if (!state.filings.contains(f)) {
        persist(FilingCreated(f)) { e =>
          log.info(s"Persisted: $f")
          updateState(e)
        }
      }

    case UpdateFilingStatus(modified) =>
      if (state.filings.map(x => x.id).contains(modified.id)) {
        persist(FilingStatusUpdated(modified)) { e =>
          log.info(s"persisted: $modified")
          updateState(e)
        }
      }

    case GetFilingByPeriod(period) =>
      val filing = state.filings.find(f => f.id == period).getOrElse(Filing())
      sender() ! filing

    case GetState =>
      sender() ! state.filings

    case Shutdown =>
      context stop self

  }
}
