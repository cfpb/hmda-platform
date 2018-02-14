package hmda.persistence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.client.ClusterClientReceptionist
import hmda.model.fi.SubmissionId
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence, SubmissionPersistence }
import hmda.persistence.model.HmdaSupervisorActor
import hmda.persistence.processing._
import hmda.persistence.messages.CommonMessages._

object HmdaSupervisor {

  val name = "supervisor"

  case class FindHmdaFiling(filingPeriod: String) extends Command
  case class FindFilings(name: String, institutionId: String) extends Command
  case class FindSubmissions(name: String, institutionId: String, period: String) extends Command
  case class FindProcessingActor(name: String, submissionId: SubmissionId) extends Command
  case class FindAPORPersistence(name: String) extends Command

  def props(validationStats: ActorRef): Props = Props(new HmdaSupervisor(validationStats))

  def createSupervisor(system: ActorSystem, validationStats: ActorRef): ActorRef = {
    system.actorOf(HmdaSupervisor.props(validationStats).withDispatcher("persistence-dispatcher"), "supervisor")
  }
}

class HmdaSupervisor(validationStats: ActorRef) extends HmdaSupervisorActor {

  import HmdaSupervisor._

  ClusterClientReceptionist(context.system).registerService(self)

  override def receive: Receive = super.receive orElse {

    case FindHmdaFiling(filingPeriod) =>
      sender() ! findHmdaFiling(filingPeriod)

    case FindFilings(name, id) =>
      sender() ! findFilings(name, id)

    case FindSubmissions(name, institutionId, period) =>
      sender() ! findSubmissions(name, institutionId, period)

    case FindProcessingActor(name, submissionId) =>
      sender() ! findProcessingActor(name, submissionId)

    case FindAPORPersistence(aporName) =>
      sender() ! findAPORPersistence(aporName)

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

  private def findAPORPersistence(name: String): ActorRef =
    actors.getOrElse(s"$name", createAPORPersistence(name))

  override def createActor(name: String): ActorRef = name match {
    case id @ SingleLarValidation.name =>
      val actor = context.actorOf(SingleLarValidation.props.withDispatcher("persistence-dispatcher"), id)
      supervise(actor, id)
    case id @ SingleTsValidation.name =>
      val actor = context.actorOf(SingleTsValidation.props.withDispatcher("persistence-dispatcher"), id)
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
    val actor = context.actorOf(SubmissionPersistence.props(institutionId, period).withDispatcher("persistence-dispatcher"), sId)
    supervise(actor, sId)
  }

  private def createAPORPersistence(name: String): ActorRef = {
    val actor = context.actorOf(HmdaAPORPersistence.props().withDispatcher("persistence-dispatcher"), name)
    supervise(actor, name)
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
      val actor = context.actorOf(HmdaFileValidator.props(self, validationStats, submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
    case id @ SubmissionManager.name =>
      val actorId = s"$id-${submissionId.toString}"
      val actor = context.actorOf(SubmissionManager.props(validationStats, submissionId).withDispatcher("persistence-dispatcher"), actorId)
      supervise(actor, actorId)
  }

}
