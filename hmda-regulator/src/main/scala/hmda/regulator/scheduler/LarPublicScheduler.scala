package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.scaladsl._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.component.RegulatorComponent2018
import hmda.regulator.query.lar.LarEntityImpl
import hmda.regulator.scheduler.schedules.Schedules.LarPublicScheduler2018
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LarPublicScheduler extends HmdaActor with RegulatorComponent2018 {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")

  def larRepository2018 = new LarRepository2018(dbConfig)

  val awsConfig = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId = awsConfig.getString("public-access-key-id")
  val secretAccess = awsConfig.getString("public-secret-access-key ")
  val region = awsConfig.getString("public-region")
  val bucket = awsConfig.getString("public-s3-bucket")
  val environment = awsConfig.getString("public-environment")
  val year = awsConfig.getString("year")
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
      .schedule("LarPublicScheduler2018", self, LarPublicScheduler2018)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2018")
  }

  override def receive: Receive = {

    case LarPublicScheduler2018 =>
      println("test timer public lar")
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileNamePSV = s"$formattedDate" + "2018_public_lar.csv"
      val fileNameCSV = s"$formattedDate" + "2018_public_lar.txt"

      val allResultsPublisher: DatabasePublisher[LarEntityImpl] =
        larRepository2018.getAllLARs(bankFilterList)

      val allResultsSource: Source[LarEntityImpl, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      //PSV Sync
      val s3SinkPSV = S3
        .multipartUpload(bucket, s"$environment/lar/$fileNamePSV")
        .withAttributes(S3Attributes.settings(s3Settings))

      var resultsPSV: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toPublicPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3SinkPSV)

      resultsPSV onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/lar/$fileNamePSV" + ".")
        }
        case Failure(t) =>
          log.info(
            "An error has occurred getting Public LAR Data in Future: " + t.getMessage)
      }

      //CSV Sync
      val s3SinkCSV = S3
        .multipartUpload(bucket, s"$environment/lar/$fileNameCSV")
        .withAttributes(S3Attributes.settings(s3Settings))

      var results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toPublicCSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3SinkCSV)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/lar/$fileNameCSV" + ".")
        }
        case Failure(t) =>
          log.info(
            "An error has occurred getting Public LAR Data in Future: " + t.getMessage)
      }
  }
}
