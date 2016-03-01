package hmda.api.processing

import akka.actor.{ Props, Actor, ActorLogging }

object MasterActor {
  def props: Props = Props(new MasterActor)
}

class MasterActor extends Actor with ActorLogging {

  override def preStart: Unit = {
    log.info(s"Master Actor started at ${self.path}")
    val printActor = context.actorOf(PrintActor.props, "print")
  }

  override def postStop: Unit = {
    log.info("Master Actor stopped")
  }

  override def receive: Receive = {
    case _ => log.warning(s"Unsupported message received by $self")
  }
}
