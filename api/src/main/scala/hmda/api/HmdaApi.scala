package hmda.api

import akka.actor.Props
import hmda.model.actor.HmdaActor

object HmdaApi {
  final val name = "HmdaApi"
  def props: Props = Props(new HmdaApi)
}

class HmdaApi extends HmdaActor {
  override def receive = super.receive orElse {
    case _ =>
  }
}
