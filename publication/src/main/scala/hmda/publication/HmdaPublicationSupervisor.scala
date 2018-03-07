package hmda.publication

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.client.ClusterClientReceptionist
import akka.util.Timeout
import hmda.persistence.model.HmdaSupervisorActor
import hmda.persistence.PersistenceConfig._
import hmda.persistence.messages.CommonMessages._
import hmda.publication.reports.aggregate.AggregateReportPublisher
import hmda.publication.reports.disclosure.DisclosureReportPublisher

import scala.concurrent.duration._

object HmdaPublicationSupervisor {

  val name = "publication-supervisor"

  case object FindDisclosurePublisher extends Command
  case object FindAggregatePublisher extends Command

  def props(): Props = Props(new HmdaPublicationSupervisor)

  def createPublicationSupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaPublicationSupervisor.props(), name)
  }
}

class HmdaPublicationSupervisor extends HmdaSupervisorActor {
  import HmdaPublicationSupervisor._

  val duration = configuration.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  override def receive: Receive = super.receive orElse {
    case FindDisclosurePublisher =>
      sender() ! findDisclosurePublisher()
    case FindAggregatePublisher =>
      sender() ! findAggregatePublisher()
    case Shutdown =>
      context stop self
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ DisclosureReportPublisher.name =>
      val actor = context.actorOf(DisclosureReportPublisher.props().withDispatcher("publication-dispatcher"), id)
      supervise(actor, id)
    case id @ AggregateReportPublisher.name =>
      val actor = context.actorOf(AggregateReportPublisher.props().withDispatcher("publication-dispatcher"), id)
      supervise(actor, id)
  }

  private def findDisclosurePublisher(): ActorRef = {
    actors.getOrElse(
      DisclosureReportPublisher.name,
      createActor(DisclosureReportPublisher.name)
    )
  }

  private def findAggregatePublisher(): ActorRef = {
    actors.getOrElse(
      AggregateReportPublisher.name,
      createActor(AggregateReportPublisher.name)
    )
  }
}
