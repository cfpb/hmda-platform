package hmda.query

import akka.actor.Props
import hmda.actor.HmdaActor

object HmdaQuery {
  final val name = "HmdaQuery"
  val props: Props = Props(new HmdaQuery)
}

class HmdaQuery extends HmdaActor {
  override def receive = super.receive orElse {
    case _ =>
  }
}
