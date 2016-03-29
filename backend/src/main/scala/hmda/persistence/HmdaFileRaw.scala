package hmda.persistence

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }

object HmdaFileRaw {
  def props(id: String): Props = Props(new HmdaFileRaw(id))

  sealed trait Command
  sealed trait Event {
    def data: String
  }
  case class AddLine(data: String) extends Command
  case class LineAdded(data: String) extends Event
  case object GetState extends Command
  case object StopActor extends Command

  case class HmdaFileRawState(events: List[String] = Nil) {
    def updated(event: Event): HmdaFileRawState = copy(event.data :: events)
    def size: Int = events.length
  }

}

class HmdaFileRaw(id: String) extends PersistentActor with ActorLogging {

  import HmdaFileRaw._

  override def persistenceId: String = s"HmdaFileRaw-$id"

  var state = HmdaFileRawState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case cmd: AddLine =>
      persist(LineAdded(cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
        context.system.eventStream.publish(e)
      }
    case GetState =>
      log.debug(state.toString)
      sender() ! state
    case StopActor =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: HmdaFileRawState) =>
      log.info("Recovering from snapshot")
      state = snapshot
  }

}
