package hmda.api.model.processing

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.messages.ProcessingStatus

object ProcessingStatusActor {
  def props(id: String): Props = Props(new ProcessingStatusActor(id))
  case object GetProcessingStatus

}

class ProcessingStatusActor(id: String) extends Actor with ActorLogging {

  import ProcessingStatusActor._

  var status: List[ProcessingStatus] = Nil

  override def receive: Receive = {
    case GetProcessingStatus =>
      sender() ! status

  }
}
