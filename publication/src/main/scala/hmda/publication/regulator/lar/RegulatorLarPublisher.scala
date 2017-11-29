package hmda.publication.regulator.lar

import java.time.LocalDateTime

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.model.{ ContentType, HttpCharsets, MediaTypes }
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{ S3Headers, ServerSideEncryption }
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.persistence.model.HmdaActor
import hmda.publication.regulator.messages._
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

object RegulatorLarPublisher {
  def props(): Props = Props(new RegulatorLarPublisher)
  def createRegulatorLARPublication(system: ActorSystem): ActorRef = {
    system.actorOf(RegulatorLarPublisher.props().withDispatcher("validation-dispatcher"), "hmda-aggregate-disclosure")
  }
}

class RegulatorLarPublisher extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  QuartzSchedulerExtension(system).schedule("LARRegulator", self, PublishRegulatorData)

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
  val environment = config.getString("hmda.publication.aws.environment")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  override def receive: Receive = {

    case PublishRegulatorData =>
      val now = LocalDateTime.now()
      val fileName = s"lar-$now.csv"
      log.info(s"Uploading $fileName to S3")
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/lar/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(ServerSideEncryption.AES256)
      )

      val headerSource = Source.fromIterator(() =>
        List(
          "id|" +
            "respondent_id|" +
            "agency_code|" +
            "loan_id|" +
            "application_date|" +
            "loan_type|" +
            "property_type|" +
            "purpose|" +
            "occupancy|" +
            "amount|" +
            "preapprovals|" +
            "action_taken_type|" +
            "action_taken_date|" +
            "msa|" +
            "state|" +
            "county|" +
            "tract|" +
            "ethnicity|" +
            "co_ethnicity|" +
            "race1|" +
            "race2|" +
            "race3|" +
            "race4|" +
            "race5|" +
            "co_race1|" +
            "co_race2|" +
            "co_race3|" +
            "co_race4|" +
            "co_race5|" +
            "sex|" +
            "co_sex|" +
            "income|" +
            "purchaser_type|" +
            "denial1|" +
            "denial2|" +
            "denial3|" +
            "rate_spread|" +
            "hoepa_status|" +
            "lien_status\n"
        ).toIterator).map(s => ByteString(s))

      val larSource = readData(fetchSize)
        .map(lar => lar.toCSV + "\n")
        .map(s => ByteString(s))

      val source = headerSource.concat(larSource)

      source.runWith(s3Sink)

    case _ => //do nothing
  }
}
