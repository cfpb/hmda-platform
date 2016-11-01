package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.persistence.model.HmdaSupervisorActor

object HmdaQuerySupervisor {
  case class FindQueryActorByName(name: String)

  def props(): Props = Props(new HmdaQuerySupervisor)

  def createQuerySupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaQuerySupervisor.props(), "query-supervisor")
  }
}

class HmdaQuerySupervisor extends HmdaSupervisorActor {

  override protected def createActor(name: String): ActorRef = name match {
    case id @ InstitutionQuery.name =>
      println(name)
      val actor = context.actorOf(InstitutionQuery.props, id)
      supervise(actor, id)
  }

}
