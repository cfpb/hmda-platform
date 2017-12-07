package hmda.publication.regulator.panel

import java.time.LocalDateTime

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
import com.typesafe.config.ConfigFactory
import hmda.persistence.model.HmdaActor
import hmda.publication.regulator.messages.PublishRegulatorData
import hmda.query.repository.institutions.InstitutionCassandraRepository

object RegulatorPanelPublisher {
  def props(): Props = Props(new RegulatorPanelPublisher)
}

class RegulatorPanelPublisher extends HmdaActor with InstitutionCassandraRepository {

  QuartzSchedulerExtension(system).schedule("PanelRegulator", self, PublishRegulatorData)

  val decider: Decider = { e =>
    log.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

  val config = ConfigFactory.load()
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
      val fileName = s"panel-$now.csv"
      log.info(s"Uploading $fileName to S3")
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/panel/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(ServerSideEncryption.AES256)
      )

      val headerSource = Source.fromIterator(() =>
        List(
          "id|" +
            "agency|" +
            "filing_period|" +
            "activity_year|" +
            "respondent_id|" +
            "institution_type|" +
            "cra|" +
            "email_domain_1|" +
            "email_domain_2|" +
            "email_domain_3|" +
            "respondent_name|" +
            "respondent_state|" +
            "respondent_city|" +
            "respondent_fips_state_number|" +
            "hmda_filer_flag|" +
            "parent_respondent_id|" +
            "parent_id_rssd|" +
            "parent_name|" +
            "parent_city|" +
            "parent_state|" +
            "assets|" +
            "other_lender_code|" +
            "top_holder_id_rssd|" +
            "top_holder_name|" +
            "top_holder_city|" +
            "top_holder_state|" +
            "top_holder_country\n"
        ).toIterator)

      val panelSource = readData(fetchSize)
        .filter(i => i.hmdaFilerFlag)
        .map(institution => institution.toCSV + "\n")
        .map(s => ByteString(s))

      val source = headerSource.map(s => ByteString(s)).concat(panelSource)
      source.runWith(s3Sink)
  }

}
