package hmda.api.model.processing

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.messages.{ ProcessingStatus, ProcessingStatusSeq }

object ProcessingStatusActor {
  def props(id: String): Props = Props(new ProcessingStatusActor(id))
  case object GetProcessingStatus

}

class ProcessingStatusActor(id: String) extends Actor with ActorLogging {

  import ProcessingStatusActor._

  var status: ProcessingStatusSeq = ProcessingStatusSeq()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[List[ProcessingStatus]])
  }

  override def receive: Receive = {
    case GetProcessingStatus =>
      sender() ! status

    case xs: ProcessingStatusSeq =>
      status = xs
      log.info(status.toString)

  }
}
