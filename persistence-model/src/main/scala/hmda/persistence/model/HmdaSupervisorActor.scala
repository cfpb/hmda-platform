package hmda.persistence.model

import akka.actor.ActorRef

object HmdaSupervisorActor {
  case class FindActorByName(name: String)
}

abstract class HmdaSupervisorActor extends HmdaActor {

  import HmdaSupervisorActor._

  var actors = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case FindActorByName(name) =>
      sender() ! findActorByName(name)
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
