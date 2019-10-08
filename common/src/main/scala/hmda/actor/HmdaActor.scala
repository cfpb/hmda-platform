package hmda.actor

import akka.actor._
import hmda.messages.CommonMessages.StopActor

abstract class HmdaActor extends Actor with ActorLogging {

  override def preStart(): Unit =
    log.debug(s"Actor started at ${self.path}")

  override def postStop(): Unit =
    log.debug(s"Actor stopped at ${self.path}")

  override def receive: Receive = {
    case StopActor => context stop self
  }

}
