package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.persistence.model.HmdaSupervisorActor
import hmda.query.view.institutions.InstitutionView

object HmdaQuerySupervisor {
  case class FindQueryActorByName(name: String)

  def props(): Props = Props(new HmdaQuerySupervisor)

  def createQuerySupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaQuerySupervisor.props(), "query-supervisor")
  }
}

class HmdaQuerySupervisor extends HmdaSupervisorActor {

  override protected def createActor(name: String): ActorRef = name match {
    case id @ InstitutionView.name =>
      val actor = context.actorOf(InstitutionView.props, id)
      supervise(actor, id)
  }

}
