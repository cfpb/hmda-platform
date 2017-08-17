package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import hmda.persistence.model.HmdaSupervisorActor
import hmda.query.projections.filing.HmdaFilingDBProjection.CreateSchema
import hmda.query.view.filing.HmdaFilingView
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef
import hmda.persistence.PersistenceConfig._
import hmda.persistence.messages.CommonMessages._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object HmdaQuerySupervisor {

  case class FindHmdaFilingView(period: String)

  def props(): Props = Props(new HmdaQuerySupervisor)

  def createQuerySupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaQuerySupervisor.props(), "query-supervisor")
  }
}

class HmdaQuerySupervisor extends HmdaSupervisorActor {
  import HmdaQuerySupervisor._

  val duration = configuration.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = context.dispatcher

  override def receive: Receive = super.receive orElse {
    case FindHmdaFilingView(period) =>
      sender() ! findHmdaFilingView(period)
    case Shutdown =>
      context stop self
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ InstitutionView.name =>
      val actor = context.actorOf(InstitutionView.props().withDispatcher("query-dispatcher"), id)
      supervise(actor, id)
  }

  private def findHmdaFilingView(period: String): ActorRef = {
    actors.getOrElse(s"HmdaFilingView-$period", createHmdaFilingView(period))
  }

  private def createHmdaFilingView(period: String)(implicit ec: ExecutionContext): ActorRef = {
    val id = s"${HmdaFilingView.name}-$period"
    val actor = context.actorOf(HmdaFilingView.props(period).withDispatcher("query-dispatcher"), id)
    for {
      p <- (actor ? GetProjectionActorRef).mapTo[ActorRef]
    } yield {
      p ! CreateSchema
    }
    supervise(actor, id)
  }

}
