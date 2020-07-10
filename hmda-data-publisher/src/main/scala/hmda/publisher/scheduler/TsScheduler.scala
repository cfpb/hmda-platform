package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.{PrivateAWSConfigLoader, SnapshotCheck}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020}
import hmda.publisher.scheduler.schedules.Schedules.{TsScheduler2018, TsScheduler2019, TsSchedulerQuarterly2020}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsScheduler extends HmdaActor with PublisherComponent2018 with PublisherComponent2019 with PublisherComponent2020  with PrivateAWSConfigLoader  {

  implicit val ec               = context.system.dispatcher
  implicit val materializer     = Materializer(context)
  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  def tsRepository2018 = new TransmittalSheetRepository2018(dbConfig)
  def tsRepository2019 = new TransmittalSheetRepository2019(dbConfig)
  def tsRepository2020 = new TransmittalSheetRepository2020(dbConfig)

  val awsConfig =
    ConfigFactory.load("application.conf").getConfig("private-aws")

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPrivate)
    .withS3RegionProvider(awsRegionProviderPrivate)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler2018", self, TsScheduler2018)
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler2019", self, TsScheduler2019)
    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2020", self, TsSchedulerQuarterly2020)

  }

  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2019")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2020")
  }

  private def uploadFileToS3(
                              s3Sink: Sink[ByteString, Future[MultipartUploadResult]],
                              transmittalSheets: => Future[Seq[TransmittalSheetEntity]]
                            ): Future[MultipartUploadResult] =
    Source
      .future(transmittalSheets)
      .mapConcat(_.toList)
      .map(transmittalSheet => transmittalSheet.toRegulatorPSV + "\n")
      .map(ByteString(_))
      .runWith(s3Sink)

  override def receive: Receive = {

    case TsScheduler2018 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName      = s"$formattedDate" + "2018_ts.txt"
       val s3Path = s"$environmentPrivate/ts/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink =
        S3.multipartUpload(bucketPrivate, fullFilePath)
          .withAttributes(S3Attributes.settings(s3Settings))

      val results: Future[MultipartUploadResult] =
        uploadFileToS3(s3Sink, tsRepository2018.getAllSheets(getFilterList()))

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +bucketPrivate+"/" + fullFilePath + ".")

        case Failure(t) =>
          log.error("An error has occurred getting TS Data 2018: " + t.getMessage)
      }

    case TsScheduler2019 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName      = s"$formattedDate" + "2019_ts.txt"
      val s3Path = s"$environmentPrivate/ts/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink =
        S3.multipartUpload(bucketPrivate, fullFilePath)
          .withAttributes(S3Attributes.settings(s3Settings))

      val results: Future[MultipartUploadResult] =
        uploadFileToS3(s3Sink, tsRepository2019.getAllSheets(getFilterList()))

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")

        case Failure(t) =>
          log.error("An error has occurred getting TS Data 2019: " + t.getMessage)
      }
    case TsSchedulerQuarterly2020 =>
      val includeQuarterly = true;
      val now              = LocalDateTime.now().minusDays(1)
      val formattedDate    = fullDateQuarterly.format(now)
      val fileName         = s"$formattedDate" + "quarterly_2020_ts.txt"
      val s3Path = s"$environmentPrivate/ts/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink =
        S3.multipartUpload(bucketPrivate, fullFilePath)
          .withAttributes(S3Attributes.settings(s3Settings))

      val results: Future[MultipartUploadResult] =
        uploadFileToS3(s3Sink, tsRepository2020.getAllSheets(getFilterList(), includeQuarterly))

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")

        case Failure(t) =>
          log.error("An error has occurred getting Quarterly TS Data 2020: " + t.getMessage)
      }
  }


}