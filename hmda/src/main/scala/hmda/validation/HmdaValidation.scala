package hmda.validation

import akka.actor.Props
import hmda.actor.HmdaActor

object HmdaValidation {
  final val name = "HmdaValidation"
  def props: Props = Props(new HmdaValidation)
}

class HmdaValidation extends HmdaActor {
  override def receive = super.receive orElse {
    case _ =>
  }
}
