package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.pattern.ask
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.Supervision._
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages.Command
import hmda.persistence.messages.commands.institutions.InstitutionCommands.{ GetInstitutionById, GetInstitutionByRespondentId }
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.PubSubTopics
import hmda.publication.regulator.lar.ModifiedLarPublisher
import hmda.publication.reports.disclosure.DisclosureReportPublisher.GenerateDisclosureReports
import hmda.publication.reports.protocol.disclosure.D5XProtocol._
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

object DisclosureReportPublisher {

  case class GenerateDisclosureReports(institutionId: String) extends Command

  val name = "SubmissionSignedDisclosureReportSubscriber"
  def props(supervisor: ActorRef): Props = Props(new ModifiedLarPublisher(supervisor))
}


class DisclosureReportPublisher(val sys: ActorSystem, val mat: ActorMaterializer) extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  val duration = config.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(duration.seconds)

  override def receive: Receive = {

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"${self.path} subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      self ! GenerateDisclosureReports(submissionId.institutionId)

    case GenerateDisclosureReports(institutionId) =>
      log.info(s"Generating disclosure reports for $institutionId")
      generateReports(institutionId)

    case _ => //do nothing
  }

  private def generateReports(institutionId: String): Future[Unit] = {
    val futures = for {
      i <- getInstitution(institutionId).mapTo[Institution]
      f <- getFipsList(i.respondentId).mapTo[Seq[Int]]
    } yield (i, f.distinct)

    futures.map(f => {
      val institution = f._1
      val fips = f._2

      fips.foreach(code => {
        generateIndividualReports(code, institution)
      })
    })
  }

  private def generateIndividualReports(fipsCode: Int, institution: Institution): Future[Unit] = {
    val larSource = readData(1000)

    val d8XReports = List(D81, D82, D83, D84, D85, D86, D87)
    val d8XF = Future.sequence(d8XReports.map { report =>
      report.generate(larSource, fipsCode, institution)
    })

    val d4XReports = List(D41, D42, D43, D44, D45, D46, D47)
    val d4XF = Future.sequence(d4XReports.map { report =>
      report.generate(larSource, fipsCode, institution)
    })

    val d51F = D51.generate(larSource, fipsCode, institution)
    d51F.map { d51 =>
      println(d51.toJson.prettyPrint)
    }

    val d53F = D53.generate(larSource, fipsCode, institution)
  }

  private def getFipsList(respondentId: String): Future[Seq[Int]] = {
    val larSource: Source[LoanApplicationRegister, NotUsed] = readData(1000)
    larSource
      .filter(lar => lar.respondentId == respondentId)
      .filter(lar => lar.geography.msa != "NA")
      .map(lar => lar.geography.msa.toInt)
      .take(Int.MaxValue)
      .runWith(Sink.seq)
  }

  private def getInstitution(institutionId: String): Future[Institution] = {
    val supervisor = system.actorSelection("/user/supervisor")
    val fInstitutionsActor = (supervisor ? FindActorByName("institutions")).mapTo[ActorRef]
    for {
      a <- fInstitutionsActor
      i <- (a ? GetInstitutionById(institutionId)).mapTo[Option[Institution]]
    } yield i.getOrElse(Institution.empty)
  }

}
