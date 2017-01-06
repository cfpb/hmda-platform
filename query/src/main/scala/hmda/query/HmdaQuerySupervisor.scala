package hmda.query

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.persistence.model.HmdaSupervisorActor
import hmda.query.projections.filing.HmdaFilingDBProjection.CreateSchema
import hmda.query.view.filing.HmdaFilingView
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef

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

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)
  implicit val ec = context.dispatcher

  override def receive: Receive = super.receive orElse {
    case FindHmdaFilingView(period) =>
      sender() ! findHmdaFilingView(period)
  }

  override protected def createActor(name: String): ActorRef = name match {
    case id @ InstitutionView.name =>
      val actor = context.actorOf(InstitutionView.props, id)
      supervise(actor, id)
  }

  private def findHmdaFilingView(period: String): ActorRef = {
    actors.getOrElse(s"HmdaFilingView-$period", createHmdaFilingView(period))
  }

  private def createHmdaFilingView(period: String)(implicit ec: ExecutionContext): ActorRef = {
    val actor = context.actorOf(HmdaFilingView.props(period), s"HmdaFilingView-$period")
    for {
      p <- (actor ? GetProjectionActorRef).mapTo[ActorRef]
    } yield {
      p ! CreateSchema
    }
    supervise(actor, s"${HmdaFilingView.name}-$period")
  }

}
