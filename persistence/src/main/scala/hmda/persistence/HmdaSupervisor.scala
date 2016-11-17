package hmda.persistence

import akka.actor.{ ActorRef, ActorSystem, Props, Terminated }
import hmda.model.fi.SubmissionId
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence, SubmissionPersistence }
import hmda.persistence.model.HmdaSupervisorActor
import hmda.persistence.processing._

object HmdaSupervisor {

  case class FindFilings(name: String, institutionId: String)
  case class FindSubmissions(name: String, institutionId: String, period: String)
  case class FindProcessingActor(name: String, submissionId: SubmissionId)

  def props(): Props = Props(new HmdaSupervisor)

  def createSupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaSupervisor.props(), "supervisor")
  }
}

class HmdaSupervisor extends HmdaSupervisorActor {

  import HmdaSupervisor._

  override def receive: Receive = super.receive orElse {

    case FindFilings(name, id) =>
      sender() ! findFilings(name, id)

    case FindSubmissions(name, institutionId, period) =>
      sender() ! findSubmissions(name, institutionId, period)

    case FindProcessingActor(name, submissionId) =>
      sender() ! findProcessingActor(name, submissionId)

    case Terminated(ref) =>
      log.debug(s"actor ${ref.path} terminated")
      actors = actors.filterNot { case (_, value) => value == ref }
  }

  private def findFilings(name: String, id: String): ActorRef =
    actors.getOrElse(s"$name-$id", createFilings(name, id))

  private def findSubmissions(name: String, institutionId: String, period: String): ActorRef =
    actors.getOrElse(s"$name-$institutionId-$period", createSubmissions(name, institutionId, period))

  private def findProcessingActor(name: String, submissionId: SubmissionId): ActorRef =
    actors.getOrElse(s"$name-${submissionId.toString}", createProcessingActor(name, submissionId))

  override def createActor(name: String): ActorRef = name match {
    case id @ SingleLarValidation.name =>
      val actor = context.actorOf(SingleLarValidation.props, id)
      supervise(actor, id)
    case id @ InstitutionPersistence.name =>
      val actor = context.actorOf(InstitutionPersistence.props, id)
      supervise(actor, id)
    case id @ LocalHmdaEventProcessor.name =>
      val actor = context.actorOf(LocalHmdaEventProcessor.props(), id)
      supervise(actor, id)
  }

  private def createFilings(name: String, id: String): ActorRef = {
    val filingsId = s"$name-$id"
    val actor = context.actorOf(FilingPersistence.props(id), filingsId)
    supervise(actor, filingsId)
  }

  private def createSubmissions(name: String, institutionId: String, period: String): ActorRef = {
    val sId = s"$name-$institutionId-$period"
    val actor = context.actorOf(SubmissionPersistence.props(institutionId, period), sId)
    supervise(actor, sId)
  }

  private def createProcessingActor(name: String, submissionId: SubmissionId): ActorRef = name match {
    case id @ HmdaRawFile.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaRawFile.props(submissionId), actorId)
      supervise(actor, actorId)
    case id @ HmdaFileParser.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaFileParser.props(submissionId), actorId)
      supervise(actor, actorId)
    case id @ HmdaFileValidator.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaFileValidator.props(submissionId), actorId)
      supervise(actor, actorId)
    case id @ SubmissionManager.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(SubmissionManager.props(submissionId), actorId)
      supervise(actor, actorId)
  }

}
