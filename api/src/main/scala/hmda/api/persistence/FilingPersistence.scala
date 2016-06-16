package hmda.api.persistence

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence._
import hmda.model.fi.{ Filing, FilingStatus }

object FilingPersistence {

  case class CreateFiling(filing: Filing) extends Command
  case class UpdateFilingStatus(filing: Filing, status: FilingStatus) extends Command

  case class FilingCreated(filing: Filing) extends Event
  case class FilingStatusUpdated(filing: Filing, status: FilingStatus) extends Event

  def props(fid: String): Props = Props(new FilingPersistence(fid))

  case class FilingState(filings: Seq[Filing] = Nil) {
    def updated(event: Event): FilingState = {
      event match {
        case FilingCreated(f) =>
          FilingState(f +: filings)
        case FilingStatusUpdated(modified, newStatus) =>
          val filing = filings.find(x => x.id == modified.id && x.fid == modified.fid).getOrElse(Filing())
          if (filing.fid != "" && filing.id != "") {
            val i = filings.indexOf(filing)
            FilingState(filings.updated(i, modified))
          } else {
            FilingState(filings)
          }
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
          updateState(e)
        }
      }

    case UpdateFilingStatus(f, s) =>
      if (state.filings.map(x => (x.id, x.fid)).contains((f.id, f.fid))) {
        persist(FilingStatusUpdated(f, s)) { e =>
          updateState(e)
        }
      }

    case GetState =>
      sender() ! state.filings

    case Shutdown =>
      context stop self

  }
}
