package hmda.publication.regulator.ts

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.{ ContentType, HttpCharsets, MediaTypes }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{ S3Headers, ServerSideEncryption }
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.persistence.model.HmdaActor
import hmda.publication.regulator.messages.PublishRegulatorData
import hmda.query.repository.filing.TransmittalSheetCassandraRepository

object RegulatorTsPublisher {
  def props(): Props = Props(new RegulatorTsPublisher)
}

class RegulatorTsPublisher extends HmdaActor with TransmittalSheetCassandraRepository {

  QuartzSchedulerExtension(system).schedule("TSRegulator", self, PublishRegulatorData)

  val decider: Decider = { e =>
    log.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

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
      val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}_ts.txt"
      log.info(s"Uploading $fileName to S3")
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/ts/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(ServerSideEncryption.AES256)
      )

      val headerSource = Source.fromIterator(() =>
        List(
          "id|" +
            "agency_code|" +
            "timestamp|" +
            "activity_year|" +
            "tax_id|" +
            "total_lines|" +
            "respondent_id|" +
            "respondent_name|" +
            "respondent_address|" +
            "respondent_city|" +
            "respondent_state|" +
            "respondent_zipcode|" +
            "parent_name|" +
            "parent_address|" +
            "parent_city|" +
            "parent_state|" +
            "parent_zipcode|" +
            "contact_name|" +
            "contact_phone|" +
            "contact_fax|" +
            "contact_email|" +
            "submission_timestamp\n"
        ).toIterator).map(s => ByteString(s))

      val tsSource = readData(fetchSize)
        .map(ts => ts.toCSV + "\n")
        .map(s => ByteString(s))

      val source = headerSource.concat(tsSource)
      source.runWith(s3Sink)

    case _ => //do nothing
  }

}
