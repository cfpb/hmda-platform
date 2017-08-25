package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.institutions.FilingPersistence._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.messages.events.institutions.FilingEvents._

object FilingPersistence {

  val name = "filings"

  case class CreateFiling(filing: Filing) extends Command
  case class UpdateFilingStatus(period: String, status: FilingStatus) extends Command
  case class GetFilingByPeriod(period: String) extends Command

  def props(institutionId: String): Props = Props(new FilingPersistence(institutionId))

  def createFilings(institutionId: String, system: ActorSystem): ActorRef = {
    system.actorOf(FilingPersistence.props(institutionId).withDispatcher("persistence-dispatcher"))
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
      if (!state.filings.map(_.period).contains(f.period)) {
        persistFilingEvent(FilingCreated(f), f)
      } else {
        sender() ! None
        log.warning(s"Could not create Filing. Filing period ${f.period} already exists for institution $institutionId.")
      }

    case UpdateFilingStatus(period, newStatus) =>
      state.filings.find(f => f.period == period) match {
        case Some(filing) =>
          val startTime = if (newStatus == InProgress) System.currentTimeMillis else filing.start
          val endTime = if (newStatus == Completed) System.currentTimeMillis else filing.end
          val updatedFiling = filing.copy(status = newStatus, start = startTime, end = endTime)
          persistFilingEvent(FilingStatusUpdated(updatedFiling), updatedFiling)
        case None =>
          sender() ! None
          log.warning(s"Could not update filing status. Institution $institutionId, filing period $period")
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

  private def persistFilingEvent(event: Event, filing: Filing) = {
    persist(event) { e =>
      log.debug(s"persisted: $filing")
      updateState(e)
      sender() ! Some(filing)
    }
  }
}
