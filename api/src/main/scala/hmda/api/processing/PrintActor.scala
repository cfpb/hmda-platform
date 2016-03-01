package hmda.api.processing

import akka.actor.{ Actor, Props, ActorLogging }
import hmda.api.processing.PrintActor.StopActor

object PrintActor {
  case object StopActor
  def props: Props = Props(new PrintActor)
}

class PrintActor extends Actor with ActorLogging {

  override def preStart: Unit = {
    log.debug(s"Print Actor started at ${self.path}")
  }

  override def receive: Receive = {
    case s: String =>
      log.debug(s)
    case StopActor =>
      context.stop(self)
    case _ => log.warning(s"Unsupported message received by $self")
  }
}
