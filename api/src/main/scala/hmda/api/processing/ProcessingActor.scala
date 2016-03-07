package hmda.api.processing

import akka.actor.{ Actor, Props, ActorLogging }
import hmda.api.processing.ProcessingActor.StopActor

object ProcessingActor {
  case object StopActor
  def props: Props = Props(new ProcessingActor)
}

class ProcessingActor extends Actor with ActorLogging {

  override def preStart: Unit = {
    log.debug(s"Processing Actor started at ${self.path}")
  }

  override def postStop: Unit = {
    log.debug(s"Processing Actor stopped at ${self.path}")
  }

  override def receive: Receive = {
    case s: String =>
      log.debug(s)
    case StopActor =>
      context.stop(self)
    case _ => log.warning(s"Unsupported message received by $self")
  }
}
