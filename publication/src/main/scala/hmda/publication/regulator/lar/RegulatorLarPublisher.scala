package hmda.publication.regulator.lar

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.{ ContentType, HttpCharsets, MediaTypes }
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{ S3Headers, ServerSideEncryption }
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.{ Flow, Source }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.census.model.{ TractExtended, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.census.model.TractLookup._
import hmda.persistence.model.HmdaActor
import hmda.publication.regulator.messages._
import hmda.query.repository.filing.LarConverter._
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

object RegulatorLarPublisher {
  def props(): Props = Props(new RegulatorLarPublisher)
  def createRegulatorLARPublication(system: ActorSystem): ActorRef = {
    system.actorOf(RegulatorLarPublisher.props().withDispatcher("validation-dispatcher"), "hmda-aggregate-disclosure")
  }
}

class RegulatorLarPublisher extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  QuartzSchedulerExtension(system).schedule("LARRegulator", self, PublishRegulatorData)
  QuartzSchedulerExtension(system).schedule("DynamicLARRegulator", self, PublishDynamicData)

  val decider: Decider = { e =>
    log.error("Unhandled error in stream", e)
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
  val publicBucket = config.getString("hmda.publication.aws.public-bucket")
  val environment = config.getString("hmda.publication.aws.environment")
  val filteredRespondentIds = config.getString("hmda.publication.filtered-respondent-ids").split(",")
  val dynamicFilteredRespondentIds = config.getString("hmda.publication.dynamic-filtered-respondent-ids").split(",")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  val tractMap = TractLookup.valuesExtended.map(t => (t.tractDec + t.county + t.state, t.toCSV)).toMap

  override def receive: Receive = {

    case PublishRegulatorData =>
      val now = LocalDateTime.now()
      val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}_lar.txt"
      log.info(s"Uploading $fileName to S3")
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/lar/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(ServerSideEncryption.AES256)
      )

      val source = readData(fetchSize)
        .via(filterTestBanks)
        .map(lar => lar.toCSV + "\n")
        .map(s => ByteString(s))

      source.runWith(s3Sink)

    case PublishDynamicData =>
      val fileName = "lar.txt"
      log.info(s"Uploading $fileName to $environment/dynamic-data/$fileName")
      val s3Sink = s3Client.multipartUpload(
        publicBucket,
        s"$environment/dynamic-data/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(ServerSideEncryption.AES256)
      )

      val source = readData(fetchSize)
        .via(filterTestBanks)
        .via(filterDynamicTestBanks)
        .map(lar => addCensusDataFromMap(lar))
        .map(s => ByteString(s))

      source.runWith(s3Sink)

    case _ => //do nothing
  }

  def addCensusDataFromMap(lar: LoanApplicationRegister): String = {
    val baseString = toModifiedLar(lar).toCSV
    val key = lar.geography.tract + lar.geography.county + lar.geography.state
    val tract = tractMap.getOrElse(key, "|||||")
    baseString + "|" + tract + "\n"
  }

  def filterTestBanks: Flow[LoanApplicationRegister, LoanApplicationRegister, NotUsed] = {
    Flow[LoanApplicationRegister]
      .filterNot(lar => filteredRespondentIds.contains(lar.respondentId) ||
        (lar.respondentId == "954623407" && lar.agencyCode == 9) ||
        (lar.respondentId == "1467" && lar.agencyCode == 1))
  }

  def filterDynamicTestBanks: Flow[LoanApplicationRegister, LoanApplicationRegister, NotUsed] = {
    Flow[LoanApplicationRegister]
      .filterNot(lar => dynamicFilteredRespondentIds.contains(lar.respondentId))
  }
}
