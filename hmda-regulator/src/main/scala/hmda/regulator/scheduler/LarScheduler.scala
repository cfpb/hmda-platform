package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.repository.LarRepository
import hmda.regulator.query.lar.LarEntityImpl
import hmda.regulator.scheduler.schedules.Schedules.LarScheduler

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LarScheduler extends HmdaActor with LarRepository {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def larRepository = new LarRepository(dbConfig)

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler", self, LarScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler")
  }

  override def receive: Receive = {

    case LarScheduler =>
      val awsConfig = ConfigFactory.load("application.conf").getConfig("aws")
      val accessKeyId = awsConfig.getString("access-key-id")
      val secretAccess = awsConfig.getString("secret-access-key ")
      val region = awsConfig.getString("region")
      val bucket = awsConfig.getString("public-bucket")
      val environment = awsConfig.getString("environment")
      val year = awsConfig.getString("year")
      val awsCredentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKeyId, secretAccess))

      val awsRegionProvider = new AwsRegionProvider {
        override def getRegion: String = region
      }

      val s3Settings = new S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )
      val bankFilter =
        ConfigFactory.load("application.conf").getConfig("filter")
      val bankFilterList = bankFilter.getString("bank-filter-list").split(",")

      val s3Client = new S3Client(s3Settings)(context.system, materializer)

      val now = LocalDateTime.now().minusDays(1)

      val formattedDate = fullDate.format(now)

      val fileName = s"$formattedDate" + s"$year" + "_lar" + ".txt"
      val s3Sink =
        s3Client.multipartUpload(bucket, s"$environment/lar/$fileName")

      val allResults: Future[Seq[LarEntityImpl]] =
        larRepository.getAllLARs(bankFilterList)

      val results: Future[MultipartUploadResult] = Source
        .fromFuture(allResults)
        .map(seek => seek.toList)
        .mapConcat(identity)
        .map(larEntity => larEntity.toPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/lar/$fileName" + ".")
        }
        case Failure(t) =>
          println("An error has occurred getting LAR Data: " + t.getMessage)
      }
  }
}
