package hmda.persistence.processing

import akka.actor.Actor.Receive
import akka.actor.{ ActorRef, Props, Terminated }

object HmdaSupervisor {
  sealed trait ActorId {
    def name: String
  }

  case object HmdaRawFileId extends ActorId {
    override def name: String = "HmdaRawFile"
  }

  case class Find(id: String)
  def props(): Props = Props(new HmdaSupervisor)
}

class HmdaSupervisor extends HmdaActor {

  import HmdaSupervisor._

  var hmdaActors = Map.empty[String, ActorRef]

  private def find(id: String): ActorRef = hmdaActors.getOrElse(id, create(id))

  private def create(id: String): ActorRef = ???

  override def receive: Receive = {
    case Find(id) =>
      sender() ! find(id)

    case Terminated(ref) =>
      log.debug(s"actor ${ref.path} terminated")
      hmdaActors = hmdaActors.filterNot { case (_, value) => value == ref }
  }
}
