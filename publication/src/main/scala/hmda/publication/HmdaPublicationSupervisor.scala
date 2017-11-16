package hmda.publication

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.client.ClusterClientReceptionist
import akka.util.Timeout
import hmda.persistence.PersistenceConfig.configuration
import hmda.persistence.messages.CommonMessages.Command
import hmda.persistence.model.HmdaSupervisorActor
import hmda.persistence.messages.CommonMessages._
import hmda.publication.submission.lar.SubmissionSignedModifiedLarSubscriber

import scala.concurrent.duration._

object HmdaPublicationSupervisor {

  val name = "publication-supervisor"

  case object FindModifiedLarSubscriber extends Command

  def props(): Props = Props(new HmdaPublicationSupervisor)
  def createPublicationSupervisor(system: ActorSystem): ActorRef =
    system.actorOf(HmdaPublicationSupervisor.props(), name)
}

class HmdaPublicationSupervisor extends HmdaSupervisorActor {
  import HmdaPublicationSupervisor._

  val duration = configuration.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = context.dispatcher

  ClusterClientReceptionist(context.system).registerService(self)

  override def receive: Receive = {

    case FindModifiedLarSubscriber =>
      sender() ! findModifiedLarSubscriber()

    case Shutdown =>
      context stop self
  }

  private def findModifiedLarSubscriber(): ActorRef = {
    actors.getOrElse(SubmissionSignedModifiedLarSubscriber.name, createActor(SubmissionSignedModifiedLarSubscriber.name))
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ SubmissionSignedModifiedLarSubscriber.name =>
      val actor = context.actorOf(
        SubmissionSignedModifiedLarSubscriber.props().withDispatcher("publication-dispatcher"),
        SubmissionSignedModifiedLarSubscriber.name
      )
      supervise(actor, id)
  }
}
