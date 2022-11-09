package hmda.publisher.scheduler

import java.time.{Clock, Instant, LocalDateTime}
import java.time.format.DateTimeFormatter
import akka.actor.typed.ActorRef
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
import hmda.publisher.qa.{QAFilePersistor, QAFileSpec, QARepository}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023, TransmittalSheetTable, TsRepository}
import hmda.publisher.scheduler.schedules.Schedule
import hmda.publisher.scheduler.schedules.Schedules.{TsScheduler2018, TsScheduler2019, TsScheduler2020, TsScheduler2021, TsScheduler2022, TsSchedulerQuarterly2020, TsSchedulerQuarterly2021, TsSchedulerQuarterly2022, TsSchedulerQuarterly2023}
import hmda.publisher.util.PublishingReporter
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Period, Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._

import scala.concurrent.Future
import scala.util.{Failure, Success}
// $COVERAGE-OFF$
class TsScheduler(publishingReporter: ActorRef[PublishingReporter.Command])
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PublisherComponent2023
    with PrivateAWSConfigLoader {

  implicit val ec               = context.system.dispatcher
  implicit val materializer     = Materializer(context)
  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")


  // Regulator File Scheduler Repos Annual
  def tsRepository2018                 = new TsRepository[TransmittalSheetTable](dbConfig, transmittalSheetTable2018)
  def tsRepository2019                 = new TsRepository[TransmittalSheetTable](dbConfig, transmittalSheetTable2019)
  def tsRepository2020                 = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Whole)
  def tsRepository2021                 = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Whole)
  def tsRepository2022                 = createTransmittalSheetRepository2022(dbConfig, Year2022Period.Whole)


  // Regulator File Scheduler Repos Quarterly
  def tsRepository2020Q1               = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Q1)
  def tsRepository2020Q2               = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Q2)
  def tsRepository2020Q3               = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Q3)

  def tsRepository2021Q1               = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Q1)
  def tsRepository2021Q2               = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Q2)
  def tsRepository2021Q3               = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Q3)

  def tsRepository2022Q1               = createTransmittalSheetRepository2022(dbConfig, Year2022Period.Q1)
  def tsRepository2022Q2               = createTransmittalSheetRepository2022(dbConfig, Year2022Period.Q2)
  def tsRepository2022Q3               = createTransmittalSheetRepository2022(dbConfig, Year2022Period.Q3)

  def tsRepository2023Q1               = createTransmittalSheetRepository2023(dbConfig, Year2023Period.Q1)
  def tsRepository2023Q2               = createTransmittalSheetRepository2023(dbConfig, Year2023Period.Q2)
  def tsRepository2023Q3               = createTransmittalSheetRepository2023(dbConfig, Year2023Period.Q3)


  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)
  val timeBarrier: QuarterTimeBarrier  = new QuarterTimeBarrier(Clock.systemDefaultZone())

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
      .schedule("TsScheduler2020", self, TsScheduler2020)
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler2021", self, TsScheduler2021)
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler2022", self, TsScheduler2022)
    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2020", self, TsSchedulerQuarterly2020)

    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2021", self, TsSchedulerQuarterly2021)

    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2022", self, TsSchedulerQuarterly2022)

    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2023", self, TsSchedulerQuarterly2023)
  }

  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2019")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2020")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2021")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2022")


    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2020")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2021")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2022")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2023")


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

    case TsScheduler2018 => publishAnnualTsData(TsScheduler2018, Period.y2018, "2018_ts.txt", tsRepository2018)

    case TsScheduler2019 => publishAnnualTsData(TsScheduler2019, Period.y2019, "2019_ts.txt", tsRepository2019)

    case TsScheduler2020 => publishAnnualTsData(TsScheduler2020, Period.y2020, "2020_ts.txt", tsRepository2020)

    case TsScheduler2021 => publishAnnualTsData(TsScheduler2021, Period.y2021, "2021_ts.txt", tsRepository2021)

    case TsScheduler2022 => publishAnnualTsData(TsScheduler2022, Period.y2022, "2022_ts.txt", tsRepository2022)

    case TsSchedulerQuarterly2020 =>
      publishQuarterTsData(TsSchedulerQuarterly2020, Period.y2020Q1, "quarter_1_2020_ts.txt", tsRepository2020Q1)
      publishQuarterTsData(TsSchedulerQuarterly2020, Period.y2020Q2, "quarter_2_2020_ts.txt", tsRepository2020Q2)
      publishQuarterTsData(TsSchedulerQuarterly2020, Period.y2020Q3, "quarter_3_2020_ts.txt", tsRepository2020Q3)

    case TsSchedulerQuarterly2021 =>
      publishQuarterTsData(TsSchedulerQuarterly2021, Period.y2021Q1, "quarter_1_2021_ts.txt", tsRepository2021Q1)
      publishQuarterTsData(TsSchedulerQuarterly2021, Period.y2021Q2, "quarter_2_2021_ts.txt", tsRepository2021Q2)
      publishQuarterTsData(TsSchedulerQuarterly2021, Period.y2021Q3, "quarter_3_2021_ts.txt", tsRepository2021Q3)

    case TsSchedulerQuarterly2022 =>
      publishQuarterTsData(TsSchedulerQuarterly2022, Period.y2022Q1, "quarter_1_2022_ts.txt", tsRepository2022Q1)
      publishQuarterTsData(TsSchedulerQuarterly2022, Period.y2022Q2, "quarter_2_2022_ts.txt", tsRepository2022Q2)
      publishQuarterTsData(TsSchedulerQuarterly2022, Period.y2022Q3, "quarter_3_2022_ts.txt", tsRepository2022Q3)
  }

  import dbConfig.profile.api._
  private def publishAnnualTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    period: Period,
    fileName: String,
    tsRepo: TsRepository[TransmittalSheetTable]): Future[Unit] = publishTsData(schedule, period, fullDate.format(LocalDateTime.now().minusDays(1)) + fileName, tsRepo)

  private def publishQuarterTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    quarter: Period.Quarter,
    fileName: String,
    tsRepo: TsRepository[TransmittalSheetTable]): Option[Future[Unit]] =
    timeBarrier.runIfStillRelevant(quarter) {
      publishTsData(schedule, quarter, fullDateQuarterly.format(LocalDateTime.now().minusDays(1)) + fileName, tsRepo)
    }

  private def publishTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    period: Period,
    fullFileName: String,
    tsRepo: TsRepository[TransmittalSheetTable]): Future[Unit] =
    publishingGuard.runIfDataIsValid(period, Scope.Private) {
      val s3Path        = s"$environmentPrivate/ts/"
      val fullFilePath  = SnapshotCheck.pathSelector(s3Path, fullFileName)

      def countF: Future[Int] = tsRepo.count()

      val results = for {
        count <- countF
        s3Sink = S3
          .multipartUpload(bucketPrivate, fullFilePath, metaHeaders = MetaHeaders(Map(LarScheduler.entriesCountMetaName -> count.toString)))
          .withAttributes(S3Attributes.settings(s3Settings))
        _ <- uploadFileToS3(s3Sink, tsRepo.getAllSheets(getFilterList()))
        count <- countF
      } yield count

      results onComplete {
        case Success(count) => reportPublishingResult(schedule, fullFilePath, Some(count))
        case Failure(t) => reportPublishingResultError(schedule, fullFilePath, t)
      }
    }


  def reportPublishingResult( schedule: Schedule, fullFilePath: String,count: Option[Int]) {

    publishingReporter ! FilePublishingCompleted(
      schedule,
      fullFilePath,
      count,
      Instant.now,
      FilePublishingCompleted.Status.Success)
    log.info(s"Pushed to S3: $bucketPrivate/$fullFilePath.")
    }

  def reportPublishingResultError( schedule: Schedule, fullFilePath: String,message:Throwable) {
    publishingReporter ! FilePublishingCompleted(
      schedule,
      fullFilePath,
      None,
      Instant.now,
      FilePublishingCompleted.Status.Success)
    log.error(s"An error has occurred while publishing $bucketPrivate/$fullFilePath: " + message.getMessage, message)
  }

}
// $COVERAGE-ON$