package hmda.publication

import java.time.LocalDateTime

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.{ ContentType, ContentTypes, MediaTypes }
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{ S3Headers, ServerSideEncryption }
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.scaladsl.Compression
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.persistence.model.HmdaActor
import hmda.publication.reports.disclosure.DisclosureReports
import hmda.query.repository.filing.FilingCassandraRepository

object HmdaPublication {
  case class GenerateDisclosureByMSAReports(respondentId: String, fipsCode: Int)
  case object PublishRegulatorData
  def props(): Props = Props(new HmdaPublication)

  def createAggregateDisclosureReports(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaPublication.props().withDispatcher("validation-dispatcher"), "hmda-aggregate-disclosure")
  }
}

class HmdaPublication extends HmdaActor with FilingCassandraRepository {

  import HmdaPublication._

  QuartzSchedulerExtension(system).schedule("Daily10PM", self, PublishRegulatorData)

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)
  override implicit val ec = context.dispatcher

  val fetchSize = config.getInt("hmda.query.fetch.size")

  val accessKeyId = config.getString("hmda.publication.aws.access-key-id")
  val secretAccess = config.getString("hmda.publication.aws.secret-access-key ")
  val region = config.getString("hmda.publication.aws.region")
  val bucket = config.getString("hmda.publication.aws.private-bucket")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  override def receive: Receive = {
    case GenerateDisclosureByMSAReports(respId, fipsCode) =>
      val disclosureReports = new DisclosureReports(system, materializer)
      disclosureReports.generateReports(fipsCode, respId)

    case PublishRegulatorData =>
      val now = LocalDateTime.now()
      val fileName = s"lar-$now.gz"
      log.info(s"Uploading $fileName to S3")
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"lar/$fileName",
        ContentType(MediaTypes.`application/x-gzip`),
        S3Headers(ServerSideEncryption.AES256)
      )
      readData(fetchSize)
        .map(lar => lar.toCSV + "\n")
        .map(s => ByteString(s))
        .via(Compression.gzip)
        .runWith(s3Sink)

    case _ => //do nothing
  }
}
