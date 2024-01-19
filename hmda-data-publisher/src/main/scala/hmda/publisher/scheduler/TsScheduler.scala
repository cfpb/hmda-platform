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
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.CronConfigLoader.{CronString, specificTsCron, specificTsYears, tsCron, tsQuarterlyCron, tsQuarterlyYears, tsYears}
import hmda.publisher.helper.{PrivateAWSConfigLoader, QuarterTimeBarrier, S3Utils, SnapshotCheck}
import hmda.publisher.query.component.{PublisherComponent, PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023, TransmittalSheetTable, TsRepository, YearPeriod}
import hmda.publisher.scheduler.schedules.{Schedule, ScheduleWithYear}
import hmda.publisher.scheduler.schedules.Schedules.{TsQuarterlySchedule, TsSchedule}
import hmda.publisher.util.{PublishingReporter, ScheduleCoordinator}
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Period, Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._

import scala.concurrent.Future
import scala.concurrent.duration.HOURS
import scala.util.{Failure, Success}
// $COVERAGE-OFF$
class TsScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
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

  val annualRepos = tsAvailableYears.map(year => year -> {
    val component = new PublisherComponent(year)
    new TsRepository[TransmittalSheetTable](dbConfig, component.transmittalSheetTableQuery(YearPeriod.Whole))
  }).toMap

  val quarterRepos = tsQuarterAvailableYears.map(year => year -> {
    val component = new PublisherComponent(year)
    (
      new TsRepository[TransmittalSheetTable](dbConfig, component.transmittalSheetTableQuery(YearPeriod.Q1)),
      new TsRepository[TransmittalSheetTable](dbConfig, component.transmittalSheetTableQuery(YearPeriod.Q2)),
      new TsRepository[TransmittalSheetTable](dbConfig, component.transmittalSheetTableQuery(YearPeriod.Q3))
    )
  }).toMap


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
    tsYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"TsSchedule_$year", self, ScheduleWithYear(TsSchedule, year), tsCron.applyOffset(idx, HOURS))
    }
    tsQuarterlyYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"TsQuarterlySchedule_$year", self, ScheduleWithYear(TsQuarterlySchedule, year), tsQuarterlyCron.applyOffset(idx, HOURS))
    }
    specificTsYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"TsSchedule_$year", self, ScheduleWithYear(TsSchedule, year), specificTsCron.applyOffset(idx, HOURS))
    }
  }

  override def postStop(): Unit = {
    tsYears.foreach(year => scheduler ! Unschedule(s"TsSchedule_$year"))
    tsQuarterlyYears.foreach(year => scheduler ! Unschedule(s"TsQuarterlySchedule_$year"))
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

    case ScheduleWithYear(schedule, year) if schedule in (TsSchedule, TsQuarterlySchedule) =>
      schedule match {
        case TsSchedule => annualRepos.get(year) match {
          case Some(repo) => publishAnnualTsData(TsSchedule, year, repo)
          case None => log.error("No available ts publisher for {}", year)
        }
        case TsQuarterlySchedule => quarterRepos.get(year) match {
          case Some((q1Repo, q2Repo, q3Repo)) =>
            publishQuarterTsData(TsQuarterlySchedule, year, YearPeriod.Q1, s"quarter_1_${year}_ts.txt", q1Repo)
            publishQuarterTsData(TsQuarterlySchedule, year, YearPeriod.Q2, s"quarter_2_${year}_ts.txt", q2Repo)
            publishQuarterTsData(TsQuarterlySchedule, year, YearPeriod.Q3, s"quarter_3_${year}_ts.txt", q3Repo)
          case None => log.error("No available ts quarterly publisher for {}", year)
        }
      }
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

  private def publishAnnualTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    year: Int,
    tsRepo: TsRepository[TransmittalSheetTable]): Future[Unit] =
    publishTsData(schedule, year, YearPeriod.Whole, fullDate.format(LocalDateTime.now().minusDays(1)) + s"${year}_ts.txt", tsRepo)

  private def publishQuarterTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    year: Int,
    quarter: YearPeriod,
    fileName: String,
    tsRepo: TsRepository[TransmittalSheetTable]): Option[Future[Unit]] =
    timeBarrier.runIfStillRelevant(year, quarter) {
      publishTsData(schedule, year, quarter, fullDateQuarterly.format(LocalDateTime.now().minusDays(1)) + fileName, tsRepo)
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

  private def publishTsData[TsTable <: Table[TransmittalSheetEntity]](
    schedule: Schedule,
    year: Int,
    period: YearPeriod,
    fullFileName: String,
    tsRepo: TsRepository[TransmittalSheetTable]): Future[Unit] =
    publishingGuard.runIfDataIsValid(year, period, Scope.Private) {
      val s3Path = s"$environmentPrivate/ts/"
      val fullFilePath = SnapshotCheck.pathSelector(s3Path, fullFileName)

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