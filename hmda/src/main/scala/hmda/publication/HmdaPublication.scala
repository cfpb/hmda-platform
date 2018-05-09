package hmda.publication

import akka.actor.Props
import hmda.actor.HmdaActor

object HmdaPublication {
  final val name = "HmdaPublication"
  def props: Props = Props(new HmdaPublication)
}

class HmdaPublication extends HmdaActor {
  override def receive = super.receive orElse {
    case _ =>
  }
}
