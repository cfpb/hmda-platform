package hmda.persistence

import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.CommonMessages.Event

abstract class HmdaPersistentActor extends PersistentActor with HmdaActor {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def updateState(event: Event): Unit

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}
