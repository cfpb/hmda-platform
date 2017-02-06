package hmda.persistence.model

import akka.actor.{ ActorRef, Terminated }

object HmdaSupervisorActor {
  case class FindActorByName(name: String)
}

abstract class HmdaSupervisorActor extends HmdaActor {

  import HmdaSupervisorActor._

  var actors = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case FindActorByName(name) =>
      sender() ! findActorByName(name)

    case Terminated(ref) =>
      log.info(s"actor ${ref.path} terminated")
      actors = actors.filterNot { case (_, value) => value == ref }
  }

  protected def createActor(name: String): ActorRef

  protected def supervise(actorRef: ActorRef, id: String): ActorRef = {
    actors += id -> actorRef
    context watch actorRef
    actorRef
  }

  protected def findActorByName(name: String): ActorRef =
    actors.getOrElse(name, createActor(name))

}
