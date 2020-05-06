package hmda.publisher.scheduler

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.ModifiedLarHeader
import hmda.publisher.query.component.PublisherComponent2018
import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.publisher.scheduler.schedules.Schedules.LarPublicScheduler2018
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class LarPublicScheduler extends HmdaActor with PublisherComponent2018 with ModifiedLarHeader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)

  def mlarRepository2018 = new ModifiedLarRepository2018(dbConfig)

  val awsConfig    = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId  = awsConfig.getString("public-access-key-id")
  val secretAccess = awsConfig.getString("public-secret-access-key ")
  val region       = awsConfig.getString("public-region")
  val bucket       = awsConfig.getString("public-s3-bucket")
  val environment  = awsConfig.getString("public-environment")
  val year         = awsConfig.getString("public-year")

  val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))

  val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProvider)
    .withS3RegionProvider(awsRegionProvider)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit =
    QuartzSchedulerExtension(context.system)
      .schedule("LarPublicScheduler2018", self, LarPublicScheduler2018)

  override def postStop(): Unit =
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2018")

  override def receive: Receive = {

    case LarPublicScheduler2018 =>
      val fileNamePSV = "2018_lar.txt"

      val allResultsPublisher: DatabasePublisher[ModifiedLarEntityImpl] =
        mlarRepository2018.getAllLARs(getFilterList())

      val allResultsSource: Source[ModifiedLarEntityImpl, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      //PSV Sync
      val s3SinkPSV = S3
        .multipartUpload(bucket, s"$environment/dynamic-data/2018/$fileNamePSV")
        .withAttributes(S3Attributes.settings(s3Settings))

      val resultsPSV: Future[MultipartUploadResult] =
        allResultsSource.zipWithIndex
          .map(mlarEntity =>
            if (mlarEntity._2 == 0)
              MLARHeader.concat(mlarEntity._1.toPublicPSV) + "\n"
            else mlarEntity._1.toPublicPSV + "\n"
          )
          .map(s => ByteString(s))
          .runWith(s3SinkPSV)

      resultsPSV onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " + s"$bucket/$environment/dynamic-data/2018/$fileNamePSV" + ".")
        case Failure(t) =>
          log.info("An error has occurred getting Public LAR Data in Future: " + t.getMessage)
      }
  }
}