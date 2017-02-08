package hmda.persistence.institutions

import akka.actor.{ActorRef, ActorSystem, Props}
import hmda.model.fi.{Completed, Filing, InProgress, NotStarted}
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.model.HmdaPersistentActor

object FilingPersistence {

  val name = "filings"

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
class FilingPersistence(institutionId: String) extends HmdaPersistentActor {

  var state = FilingState()

  override def updateState(e: Event): Unit = {
    state = state.updated(e)
  }

  override def persistenceId: String = s"$name-$institutionId"

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case CreateFiling(f) =>
      if (!state.filings.contains(f)) {
        persist(FilingCreated(f)) { e =>
          log.debug(s"Persisted: $f")
          updateState(e)
          sender() ! Some(f)
        }
      } else {
        sender() ! None
        log.warning(s"Filing already exists. Could not create $f")
      }

    case UpdateFilingStatus(modified) =>
      if (state.filings.map(x => x.period).contains(modified.period)) {
        val start = if (modified.status == InProgress) {
          System.currentTimeMillis
        } else {
          modified.start
        }
        val end = if (modified.status == Completed) {
          System.currentTimeMillis
        } else {
          modified.end
        }
        val withTimes = modified.copy(start = start, end = end)
        persist(FilingStatusUpdated(withTimes)) { e =>
          log.debug(s"persisted: $withTimes")
          updateState(e)
          sender() ! Some(withTimes)
        }
      } else {
        sender() ! None
        log.warning(s"Period does not exist. Could not update $modified")
      }

    case GetFilingByPeriod(period) =>
      val filing = state.filings.find(f => f.period == period).getOrElse(Filing("", institutionId, NotStarted, false, 0L, 0L))
      // TODO: How should we initialize the filingRequired value for the empty filing?
      if (state.filings.isEmpty)
        sender() ! Filing()
      else
        sender() ! filing

    case GetState =>
      sender() ! state.filings

  }
}
