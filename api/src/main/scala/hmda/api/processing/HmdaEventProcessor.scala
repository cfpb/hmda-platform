package hmda.api.processing

import scala.concurrent.duration._
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.util.Timeout
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.{ HmdaRawFile, HmdaRawFileParser }
import hmda.persistence.processing.HmdaRawFile.{ AddLine, UploadCompleted, UploadStarted }
import hmda.persistence.processing.HmdaRawFileParser.{ StartParsingHmdaFile, ParsingHmdaFileCompleted }

object HmdaEventProcessor {
  def props: Props = Props(new HmdaEventProcessor)

  def createHmdaEventProcessor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaEventProcessor.props, "hmda-event-processor")
  }
}

class HmdaEventProcessor extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)

  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    log.info(s"Event Processor started at ${self.path}")
    context.system.eventStream.subscribe(self, classOf[Event])
  }

  override def receive: Receive = {

    case d @ AddLine(timestamp, data, id) =>
      context.actorSelection(s"/user/hmda-event-processor/${HmdaRawFile.name}-$id") ! AddLine(timestamp, data, id)

    //Events

    case e: Event => e match {
      case UploadStarted(submissionId) =>
        log.info(s"Upload started for submission $submissionId")
        context.actorOf(HmdaRawFile.props(submissionId), s"${HmdaRawFile.name}-$submissionId")

      case UploadCompleted(submissionId) =>
        fireUploadCompletedEvents(submissionId)

      case ParsingHmdaFileCompleted(submissionId) =>
        context.actorSelection(s"/user/hmda-event-processor/${HmdaRawFileParser.name}-$submissionId").resolveOne().map { actorRef =>
          actorRef ! Shutdown
        }

      case _ => //ignore any other type of event
    }

  }

  private def fireUploadCompletedEvents(submissionId: String): Unit = {
    context.actorSelection(s"/user/hmda-event-processor/${HmdaRawFile.name}-$submissionId").resolveOne().map { actorRef =>
      actorRef ! Shutdown
    }

    val hmdaFileStreaming = context.actorOf(HmdaRawFileParser.props(submissionId), s"${HmdaRawFileParser.name}-$submissionId")
    hmdaFileStreaming ! StartParsingHmdaFile()
    log.info(s"Upload completed for submission $submissionId")
  }

  private def publishEvent(e: Event): Unit = {
    context.system.eventStream.publish(e)
  }

}
