package hmda.api.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaRawFile
import hmda.persistence.processing.HmdaRawFile.{ AddLine, UploadCompleted, UploadStarted }

object HmdaEventProcessor {
  def props: Props = Props(new HmdaEventProcessor)

  def createHmdaEventProcessor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaEventProcessor.props, "hmda-event-processor")
  }
}

class HmdaEventProcessor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"Event Processor started at ${self.path}")
    context.system.eventStream.subscribe(self, classOf[Event])
  }

  override def receive: Receive = {

    case d @ AddLine(timestamp, data, id) =>
      context.actorSelection(s"/user/hmda-event-processor/${HmdaRawFile.name}-$id") ! AddLine(timestamp, data, id)

    //Events

    case e: Event => e match {
      case UploadStarted(id) =>
        log.info(s"Upload started for submission $id")
        context.actorOf(HmdaRawFile.props(id), s"${HmdaRawFile.name}-$id")

      case UploadCompleted(id) =>
        context.actorSelection(s"/user/hmda-event-processor/${HmdaRawFile.name}-$id") ! Shutdown
        log.info(s"Upload completed for submission $id")

      case _ => //ignore any other type of event
    }

  }
}
