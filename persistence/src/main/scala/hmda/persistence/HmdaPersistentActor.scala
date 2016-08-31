package hmda.persistence

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.CommonMessages.Event

abstract class HmdaPersistentActor extends PersistentActor with ActorLogging {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def preStart(): Unit = {
    log.debug(s"Actor started at ${self.path}")
  }

  override def postStop(): Unit = {
    log.debug(s"Actor stopped at ${self.path}")
  }

  def updateState(event: Event): Unit

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}
