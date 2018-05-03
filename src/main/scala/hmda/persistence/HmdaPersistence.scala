package hmda.persistence

import akka.actor.Props
import hmda.model.actor.HmdaActor

object HmdaPersistence {
  final val name = "HmdaPersistence"
  def props: Props = Props(new HmdaPersistence)
}
class HmdaPersistence extends HmdaActor {
  override def receive = super.receive orElse {
    case _ =>
  }
}
