package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.persistence.model.HmdaSupervisorActor
import hmda.query.view.filing.HmdaFilingView
import hmda.query.view.institutions.InstitutionView

object HmdaQuerySupervisor {

  case class FindHmdaFilingView(period: String)

  def props(): Props = Props(new HmdaQuerySupervisor)

  def createQuerySupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaQuerySupervisor.props(), "query-supervisor")
  }
}

class HmdaQuerySupervisor extends HmdaSupervisorActor {
  import HmdaQuerySupervisor._

  override def receive: Receive = super.receive orElse {
    case m @ FindHmdaFilingView(period) =>
      sender() ! findHmdaFilingView(m)
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ InstitutionView.name =>
      val actor = context.actorOf(InstitutionView.props, id)
      supervise(actor, id)
  }

  protected def findHmdaFilingView(view: FindHmdaFilingView): ActorRef = {
    val period = view.period
    val actor = context.actorOf(HmdaFilingView.props(period), s"HmdaFilingView-$period")
    supervise(actor, s"${HmdaFilingView.name}-$period")
  }

}
