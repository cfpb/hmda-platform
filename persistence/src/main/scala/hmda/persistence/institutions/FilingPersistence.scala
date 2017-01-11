package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.{ Filing, NotStarted }
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
      println(s"    This is the event updating: $event")
      event match {
        case FilingCreated(f) =>
          println(s"    Filing state sees new filing created: $f")
          FilingState(f +: filings)
        case FilingStatusUpdated(modified) =>
          val x = filings.find(x => x.period == modified.period).getOrElse(Filing())
          val i = filings.indexOf(x)
          println(s"    Filing state sees filing updated: $modified")
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
    println(s"      New state is $state")
  }

  override def persistenceId: String = s"$name-$institutionId"

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case CreateFiling(f) =>
      println(s"  Filing persistence $persistenceId creates: $f")
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
        persist(FilingStatusUpdated(modified)) { e =>
          log.debug(s"persisted: $modified")
          println(s"  Filing persistence $persistenceId updates: $state")
          updateState(e)
          println(s"  Filing persistence $persistenceId is updated: $state")
          sender() ! Some(modified)
        }
      } else {
        sender() ! None
        log.warning(s"Period does not exist. Could not update $modified")
      }

    case GetFilingByPeriod(period) =>
      val filing = state.filings.find(f => f.period == period).getOrElse(Filing())
      println(s"  Filing persistence $persistenceId gets by period: $state")
      println(s"    Period is $period")

      // TODO: How should we initialize the filingRequired value for the empty filing?
      if (state.filings.isEmpty)
        sender() ! Filing()
      else
        sender() ! filing

    case GetState =>
      println(s"  Filing persistence $persistenceId gets state: $state")
      sender() ! state.filings

  }
}
