package hmda.api.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import hmda.api.persistence.CommonMessages.Command
import hmda.api.persistence.HmdaFileUpload
import hmda.api.processing.ProcessingActor.ProcessLine

object ProcessingActor {
  case class ProcessLine(timestamp: Long, data: String) extends Command

  def props(id: String, filingPeriod: String, submissionId: String): Props = {
    Props(new ProcessingActor(id, filingPeriod, submissionId))
  }

  def createProcessing(id: String, filingPeriod: String, submissionId: String, system: ActorSystem): ActorRef = {
    system.actorOf(this.props(id, filingPeriod, submissionId))
  }
}

//Processing Actor for a particular submission
class ProcessingActor(fid: String, filingPeriod: String, submissionId: String) extends Actor with ActorLogging {

  val uploadActor = context.actorOf(HmdaFileUpload.props(s"$fid-$filingPeriod-$submissionId"))

  override def receive: Receive = {

    case ProcessLine(timestamp, data) =>
      log.info(data)

    case _ => log.warning(s"Message not recognized")
  }
}
