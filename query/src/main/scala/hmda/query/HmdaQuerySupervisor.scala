package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.client.ClusterClientReceptionist
import akka.util.Timeout
import hmda.persistence.model.HmdaSupervisorActor
import hmda.query.view.filing.HmdaFilingView
import hmda.persistence.PersistenceConfig._
import hmda.persistence.messages.CommonMessages._
import hmda.query.projections.filing.SubmissionSignedEventQuerySubscriber

import scala.concurrent.duration._

object HmdaQuerySupervisor {

  val name = "query-supervisor"

  case class FindHmdaFilingView(period: String) extends Command
  case object FindSignedEventQuerySubscriber extends Command

  def props(): Props = Props(new HmdaQuerySupervisor)

  def createQuerySupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaQuerySupervisor.props(), name)
  }
}

class HmdaQuerySupervisor extends HmdaSupervisorActor {
  import HmdaQuerySupervisor._

  val duration = configuration.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  override def receive: Receive = super.receive orElse {
    case FindHmdaFilingView(period) =>
      sender() ! findHmdaFilingView(period)
    case FindSignedEventQuerySubscriber =>
      sender() ! findSignedEventQuerySubscriber()
    case Shutdown =>
      context stop self
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ SubmissionSignedEventQuerySubscriber.name =>
      val actor = context.actorOf(SubmissionSignedEventQuerySubscriber.props().withDispatcher("query-dispatcher"))
      supervise(actor, id)
  }

  private def findHmdaFilingView(period: String): ActorRef = {
    actors.getOrElse(s"HmdaFilingView-$period", createHmdaFilingView(period))
  }

  private def createHmdaFilingView(period: String): ActorRef = {
    val id = s"${HmdaFilingView.name}-$period"
    val actor = context.actorOf(HmdaFilingView.props(period).withDispatcher("query-dispatcher"), id)
    supervise(actor, id)
  }

  private def findSignedEventQuerySubscriber(): ActorRef = {
    actors.getOrElse(SubmissionSignedEventQuerySubscriber.name, createActor(SubmissionSignedEventQuerySubscriber.name))
  }

}
