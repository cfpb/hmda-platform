package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{
  MemoryBufferType,
  MultipartUploadResult,
  S3Attributes,
  S3Settings
}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.component.{
  RegulatorComponent2018,
  RegulatorComponent2019
}
import hmda.regulator.query.lar.LarEntityImpl
import hmda.regulator.scheduler.schedules.Schedules.{
  LarScheduler2018,
  LarScheduler2019
}
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LarScheduler
    extends HmdaActor
    with RegulatorComponent2018
    with RegulatorComponent2019 {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")

  def larRepository2018 = new LarRepository2018(dbConfig)
  def larRepository2019 = new LarRepository2019(dbConfig)

  val awsConfig =
    ConfigFactory.load("application.conf").getConfig("private-aws")
  val accessKeyId = awsConfig.getString("private-access-key-id")
  val secretAccess = awsConfig.getString("private-secret-access-key ")
  val region = awsConfig.getString("private-region")
  val bucket = awsConfig.getString("private-s3-bucket")
  val environment = awsConfig.getString("private-environment")
  val year = awsConfig.getString("private-year")
  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val s3Settings = S3Settings(
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
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler2018", self, LarScheduler2018)
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler2019", self, LarScheduler2019)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler2019")

  }

  override def receive: Receive = {

    case LarScheduler2018 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName = s"$formattedDate" + "2018_lar.txt"
      val s3Sink = S3
        .multipartUpload(bucket, s"$environment/lar/$fileName")
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl] =
        larRepository2018.getAllLARs(bankFilterList)
      val allResultsSource: Source[LarEntityImpl, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      var results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/lar/$fileName" + ".")
        }
        case Failure(t) =>
          log.info(
            "An error has occurred getting LAR Data in Future: " + t.getMessage)
      }

    case LarScheduler2019 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName = s"$formattedDate" + "2019_lar.txt"
      val s3Sink = S3
        .multipartUpload(bucket, s"$environment/lar/$fileName")
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl] =
        larRepository2019.getAllLARs(bankFilterList)
      val allResultsSource: Source[LarEntityImpl, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      var results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/lar/$fileName" + ".")
        }
        case Failure(t) =>
          log.info(
            "An error has occurred getting LAR Data 2019 in Future: " + t.getMessage)
      }
  }
}
