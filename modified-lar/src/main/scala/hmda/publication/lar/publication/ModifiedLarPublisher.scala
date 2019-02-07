package hmda.publication.lar.publication

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
import hmda.publication.lar.parser.ModifiedLarCsvParser
import hmda.query.HmdaQuery._

sealed trait ModifiedLarCommand
case class UploadToS3(submissionId: SubmissionId) extends ModifiedLarCommand

object ModifiedLarPublisher {

  final val name: String = "ModifiedLarPublisher"

  val config = ConfigFactory.load()

  val accessKeyId = config.getString("aws.access-key-id")
  val secretAccess = config.getString("aws.secret-access-key ")
  val region = config.getString("aws.region")
  val bucket = config.getString("aws.public-bucket")
  val environment = config.getString("aws.environment")
  val year = config.getInt("hmda.lar.modified.year")
  val bankFilter = ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")
  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val behavior: Behavior[ModifiedLarCommand] =
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
        true,
        None,
        ListBucketVersion2
      )

      val s3Client = new S3Client(s3Settings, system, materializer)

      Behaviors.receiveMessage {

        case UploadToS3(submissionId) =>
          log.info(s"Publishing Modified LAR for $submissionId")

          val fileName = s"${submissionId.lei}.txt"

          val s3Sink = s3Client.multipartUpload(
            bucket,
            s"$environment/modified-lar/$year/$fileName")

          readRawData(submissionId)
            .map(l => l.data)
            .drop(1)
            .map(s => ModifiedLarCsvParser(s).toCSV + "\n")
            .map(s => ByteString(s))
            .runWith(s3Sink)

          Behaviors.same
        case _ =>
          Behaviors.ignore
      }
    }
}
