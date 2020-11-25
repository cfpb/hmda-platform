package hmda.publisher.scheduler

import java.time.{Clock, LocalDateTime}
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
import hmda.publisher.helper.{PrivateAWSConfigLoader, QuarterTimeBarrier, S3Utils, SnapshotCheck}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020}
import hmda.publisher.scheduler.schedules.Schedules.{TsScheduler2018, TsScheduler2019, TsScheduler2020, TsSchedulerQuarterly2020}
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Period, Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsScheduler
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PrivateAWSConfigLoader {

  implicit val ec               = context.system.dispatcher
  implicit val materializer     = Materializer(context)
  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  def tsRepository2018                 = new TransmittalSheetRepository2018(dbConfig)
  def tsRepository2019                 = new TransmittalSheetRepository2019(dbConfig)
  def tsRepository2020                 = new TransmittalSheetRepository2020(dbConfig)
  def tsRepository2020Q1               = new TransmittalSheetRepository2020Q1(dbConfig)
  def tsRepository2020Q2               = new TransmittalSheetRepository2020Q2(dbConfig)
  def tsRepository2020Q3               = new TransmittalSheetRepository2020Q3(dbConfig)
  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)
  val timeBarrier: QuarterTimeBarrier = new QuarterTimeBarrier(Clock.systemDefaultZone())

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
                            ): Future[MultipartUploadResult] = {
    val source = Source
      .future(transmittalSheets)
      .mapConcat(_.toList)
      .map(transmittalSheet => transmittalSheet.toRegulatorPSV + "\n")
      .map(ByteString(_))
    S3Utils.uploadWithRetry(source, s3Sink)
  }

  override def receive: Receive = {

    case TsScheduler2018 =>
      publishingGuard.runIfDataIsValid(Period.y2018, Scope.Private) {
        val now           = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName      = s"$formattedDate" + "2018_ts.txt"
        val s3Path        = s"$environmentPrivate/ts/"
        val fullFilePath  = SnapshotCheck.pathSelector(s3Path, fileName)

        val s3Sink =
          S3.multipartUpload(bucketPrivate, fullFilePath)
            .withAttributes(S3Attributes.settings(s3Settings))

        val results: Future[MultipartUploadResult] =
          uploadFileToS3(s3Sink, tsRepository2018.getAllSheets(getFilterList()))

        results onComplete {
          case Success(result) =>
            log.info("Pushed to S3: " + bucketPrivate + "/" + fullFilePath + ".")

          case Failure(t) =>
            log.error("An error has occurred getting TS Data 2018: " + t.getMessage)
        }
      }

    case TsScheduler2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Private) {
        val now           = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName      = s"$formattedDate" + "2019_ts.txt"
        val s3Path        = s"$environmentPrivate/ts/"
        val fullFilePath  = SnapshotCheck.pathSelector(s3Path, fileName)

        val s3Sink =
          S3.multipartUpload(bucketPrivate, fullFilePath)
            .withAttributes(S3Attributes.settings(s3Settings))

        val results: Future[MultipartUploadResult] =
          uploadFileToS3(s3Sink, tsRepository2019.getAllSheets(getFilterList()))

        results onComplete {
          case Success(result) =>
            log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" + ".")

          case Failure(t) =>
            log.error("An error has occurred getting TS Data 2019: " + t.getMessage)
        }
      }

    case TsScheduler2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Private) {
        val now           = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName      = s"$formattedDate" + "2020_ts.txt"
        val s3Path        = s"$environmentPrivate/ts/"
        val fullFilePath  = SnapshotCheck.pathSelector(s3Path, fileName)

        val s3Sink =
          S3.multipartUpload(bucketPrivate, fullFilePath)
            .withAttributes(S3Attributes.settings(s3Settings))

        val results: Future[MultipartUploadResult] =
          uploadFileToS3(s3Sink, tsRepository2019.getAllSheets(getFilterList()))

        results onComplete {
          case Success(result) =>
            log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" + ".")

          case Failure(t) =>
            log.error("An error has occurred getting TS Data 2020: " + t.getMessage)
        }
      }
    case TsSchedulerQuarterly2020 =>
      val includeQuarterly = true;
      val now              = LocalDateTime.now().minusDays(1)
      val formattedDate    = fullDateQuarterly.format(now)
      val s3Path           = s"$environmentPrivate/ts/"
      def publishQuarter[Table <: TransmittalSheetTableBase](quarter: Period.Quarter, repo: TSRepository2020Base[Table], fileNameSuffix: String) = {
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {
            val fileName = formattedDate + fileNameSuffix
            val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)
            val s3Sink =
              S3.multipartUpload(bucketPrivate, fullFilePath)
                .withAttributes(S3Attributes.settings(s3Settings))

            def data: Future[Seq[TransmittalSheetEntity]] =
              repo.getAllSheets(getFilterList())

            val results: Future[MultipartUploadResult] =
              uploadFileToS3(s3Sink, data)
            results onComplete {
              case Success(result) =>
                log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" + ".")
              case Failure(t) =>
                log.error("An error has occurred getting Quarterly TS Data 2020: " + t.getMessage)
            }
          }
        }
      }
      publishQuarter(Period.y2020Q1, tsRepository2020Q1, "_quarter_1_2020_ts.txt")
      publishQuarter(Period.y2020Q2, tsRepository2020Q2, "_quarter_2_2020_ts.txt")
      publishQuarter(Period.y2020Q3, tsRepository2020Q3, "_quarter_3_2020_ts.txt")

  }

}