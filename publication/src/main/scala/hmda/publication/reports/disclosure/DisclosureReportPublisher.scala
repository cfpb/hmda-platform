package hmda.publication.reports.disclosure

import java.util.concurrent.CompletionStage

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.pattern.ask
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.Supervision._
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.{ ByteString, Timeout }
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.messages.commands.institutions.InstitutionCommands.GetInstitutionById
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SubmissionManager.GetActorRef
import hmda.persistence.processing.{ PubSubTopics, SubmissionManager }
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository
import hmda.validation.messages.ValidationStatsMessages.FindIrsStats
import hmda.validation.stats.SubmissionLarStats
import akka.stream.alpakka.s3.javadsl.MultipartUploadResult
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.messages.commands.disclosure.DisclosureCommands.GenerateDisclosureReports

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._

object DisclosureReportPublisher {
  val name = "SubmissionSignedDisclosureReportSubscriber"
  def props(supervisor: ActorRef): Props = Props(new DisclosureReportPublisher(supervisor))
}

class DisclosureReportPublisher(supervisor: ActorRef) extends HmdaActor with LoanApplicationRegisterCassandraRepository {

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

  val accessKeyId = config.getString("hmda.publication.aws.access-key-id")
  val secretAccess = config.getString("hmda.publication.aws.secret-access-key ")
  val region = config.getString("hmda.publication.aws.region")
  val bucket = config.getString("hmda.publication.aws.public-bucket")
  val environment = config.getString("hmda.publication.aws.environment")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  /*val reports = List(
    D41, D42, D43, D44, D45, D46, D47,
    D51, D52, D53,
    D71, D72, D73, D74, D75, D76, D77,
    D81, D82, D83, D84, D85, D86, D87,
    D11_1, D11_2, D11_3, D11_4, D11_5, D11_6, D11_7, D11_8, D11_9, D11_10,
    DiscB
  )*/

  val reports = List(
    D41, D42, D43, D44, D45, D46, D47
  )

  override def receive: Receive = {

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"${self.path} subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      self ! GenerateDisclosureReports(submissionId)

    case GenerateDisclosureReports(submissionId) =>
      log.info(s"Generating disclosure reports for ${submissionId.toString}")
      generateReports(submissionId)

    case _ => //do nothing
  }

  private def generateReports(submissionId: SubmissionId): Future[Unit] = {
    val futures = for {
      i <- getInstitution(submissionId.institutionId).mapTo[Institution]
      irs <- getMSAFromIRS(submissionId)
    } yield (i, irs)

    futures.map(f => {
      val institution = f._1
      val msaList = f._2.toList

      val reportFlow: Flow[Int, DisclosureReportPayload, NotUsed] =
        Flow[Int]
          .mapAsync(1)(msa => generateIndividualReports(msa, institution))
          .mapConcat(identity)

      val s3Flow: Flow[DisclosureReportPayload, CompletionStage[MultipartUploadResult], NotUsed] =
        Flow[DisclosureReportPayload]
          .map(payload => {
            val filePath = s"$environment/reports/disclosure/${submissionId.institutionId}/${payload.msa}/${payload.reportID}.txt"
            Source.single(ByteString(payload.report))
              .runWith(s3Client.multipartUpload(bucket, filePath))
          })

      Source(msaList).via(reportFlow).via(s3Flow).runWith(Sink.foreach(println))
    })
  }

  private def generateIndividualReports(
    msa: Int,
    institution: Institution
  ): Future[List[DisclosureReportPayload]] = {
    log.info(s"Generating reports for ${institution.respondent.name} in the MSA $msa")
    Future.sequence(reports.map(report => {
      val larSource = readData(1000)
      report.generate(larSource, msa, institution)
    }))
    /*var fSerialize: Future[List[DisclosureReportPayload]] = Future[List[DisclosureReportPayload]]{()}
    reports.foreach(report => {
      val larSource = readData(1000)
      fSerialize = fSerialize.flatMap{_ => report.generate(larSource, msa, institution})
    })
    fSerialize*/
  }

  private def getInstitution(institutionId: String): Future[Institution] = {
    val supervisor = system.actorSelection("/user/supervisor/singleton")
    val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    for {
      a <- fInstitutionsActor
      i <- (a ? GetInstitutionById(institutionId)).mapTo[Option[Institution]]
    } yield {
      i.getOrElse(Institution.empty)
    }
  }

  private def getMSAFromIRS(submissionId: SubmissionId): Future[Seq[Int]] = {
    val supervisor = system.actorSelection("/user/supervisor/singleton")
    for {
      manager <- (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
      larStats <- (manager ? GetActorRef(SubmissionLarStats.name)).mapTo[ActorRef]
      stats <- (larStats ? FindIrsStats(submissionId)).mapTo[Seq[Msa]]
    } yield {
      stats.filter(m => m.id != "NA").map(m => m.id.toInt)
    }
  }

}
