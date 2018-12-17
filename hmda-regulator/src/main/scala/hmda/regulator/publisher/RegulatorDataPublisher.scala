package hmda.regulator.publisher

import java.time.LocalDateTime
import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId
import hmda.regulator.data.RegulatorDataEntity
import hmda.query.HmdaQuery.

sealed trait RegulatorDataPublisher
case class UploadToS3(regulatorDataEntity: RegulatorDataEntity)
    extends RegulatorDataPublisher

object RegulatorDataPublisher {

  final val name: String = "RegulatorDataPublisher"

  val config = ConfigFactory.load()

  val accessKeyId = config.getString("aws.access-key-id")
  val secretAccess = config.getString("aws.secret-access-key ")
  val region = config.getString("aws.region")
  val bucket = config.getString("aws.public-bucket")
  val environment = config.getString("aws.environment")
  val year = config.getInt("hmda.lar.modified.year")

  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val behavior: Behavior[RegulatorDataPublisher] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      val decider: Supervision.Decider = {
        case e: Throwable =>
          log.error(e.getLocalizedMessage)
          Supervision.Resume
      }
      implicit val system = ctx.system.toUntyped
      implicit val materializer = ActorMaterializer(
        ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      log.info(s"Started $name")

      val s3Settings = new S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )

      val s3Client = new S3Client(s3Settings, system, materializer)

      Behaviors.receiveMessage {

        case UploadToS3(regulatorDataEntity) =>
          val now = LocalDateTime.now()
          val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}" + "_" + regulatorDataEntity.dataType + ".txt"

          log.info(s"Uploading Regulator Data file : $fileName" + "  to S3.")

          val s3Sink =
            s3Client.multipartUpload(bucket,
                                     s"$environment/regulator+/$year/$fileName")

          readRawData(regulatorDataEntity)
            .map(l => l.data)
            .drop(1)
            .map(s => regulatorDataEntity.toCSV + "\n")
            .map(s => ByteString(s))
            .runWith(s3Sink)

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}
