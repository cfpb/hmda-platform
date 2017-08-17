package hmda.persistence

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence, SubmissionPersistence }
import hmda.persistence.model.HmdaSupervisorActor
import hmda.persistence.processing._
import hmda.persistence.messages.CommonMessages._

object HmdaSupervisor {

  case class FindHmdaFiling(filingPeriod: String)
  case class FindFilings(name: String, institutionId: String)
  case class FindSubmissions(name: String, institutionId: String, period: String)
  case class FindProcessingActor(name: String, submissionId: SubmissionId)

  def props(): Props = Props(new HmdaSupervisor)

  def createSupervisor(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaSupervisor.props().withDispatcher("persistence-dispatcher"), "supervisor")
  }
}

class HmdaSupervisor extends HmdaSupervisorActor {

  import HmdaSupervisor._

  override def receive: Receive = super.receive orElse {

    case FindHmdaFiling(filingPeriod) =>
      sender() ! findHmdaFiling(filingPeriod)

    case FindFilings(name, id) =>
      sender() ! findFilings(name, id)

    case FindSubmissions(name, institutionId, period) =>
      sender() ! findSubmissions(name, institutionId, period)

    case FindProcessingActor(name, submissionId) =>
      sender() ! findProcessingActor(name, submissionId)

    case Shutdown => context stop self

  }

  private def findHmdaFiling(filingPeriod: String) =
    actors.getOrElse(s"${HmdaFiling.name}", createHmdaFiling(filingPeriod))

  private def findFilings(name: String, id: String): ActorRef =
    actors.getOrElse(s"$name-$id", createFilings(name, id))

  private def findSubmissions(name: String, institutionId: String, period: String): ActorRef =
    actors.getOrElse(s"$name-$institutionId-$period", createSubmissions(name, institutionId, period))

  private def findProcessingActor(name: String, submissionId: SubmissionId): ActorRef =
    actors.getOrElse(s"$name-${submissionId.toString}", createProcessingActor(name, submissionId))

  override def createActor(name: String): ActorRef = name match {
    case id @ SingleLarValidation.name =>
      val actor = context.actorOf(SingleLarValidation.props.withDispatcher("persistence-dispatcher"), id)
      supervise(actor, id)
    case id @ InstitutionPersistence.name =>
      val actor = context.actorOf(InstitutionPersistence.props.withDispatcher("persistence-dispatcher"), id)
      supervise(actor, id)

  }

  private def createHmdaFiling(filingPeriod: String): ActorRef = {
    val actor = context.actorOf(
      HmdaFiling.props(filingPeriod).withDispatcher("persistence-dispatcher"),
      s"${HmdaFiling.name}-$filingPeriod"
    )
    supervise(actor, HmdaFiling.name)
  }

  private def createFilings(name: String, id: String): ActorRef = {
    val filingsId = s"$name-$id"
    val actor = context.actorOf(FilingPersistence.props(id).withDispatcher("persistence-dispatcher"), filingsId)
    supervise(actor, filingsId)
  }

  private def createSubmissions(name: String, institutionId: String, period: String): ActorRef = {
    val sId = s"$name-$institutionId-$period"
    val actor = context.actorOf(SubmissionPersistence
      .props(institutionId, period)
      .withDispatcher("persistence-dispatcher"), sId)
    supervise(actor, sId)
  }

  private def createProcessingActor(name: String, submissionId: SubmissionId): ActorRef = name match {
    case id @ HmdaRawFile.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaRawFile.props(submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
    case id @ HmdaFileParser.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaFileParser.props(submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
    case id @ HmdaFileValidator.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(HmdaFileValidator.props(submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
    case id @ SubmissionManager.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(SubmissionManager.props(submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
  }

}
