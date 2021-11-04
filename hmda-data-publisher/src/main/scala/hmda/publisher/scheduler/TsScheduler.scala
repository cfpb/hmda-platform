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
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022}
import hmda.publisher.scheduler.schedules.Schedule
import hmda.publisher.scheduler.schedules.Schedules.{TsScheduler2018, TsScheduler2019, TsScheduler2020, TsScheduler2021, TsSchedulerQuarterly2020, TsSchedulerQuarterly2021, TsSchedulerQuarterly2022}
import hmda.publisher.util.PublishingReporter
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Period, Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
// $COVERAGE-OFF$
class TsScheduler(publishingReporter: ActorRef[PublishingReporter.Command], qaFilePersistor: QAFilePersistor)
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PrivateAWSConfigLoader {

  implicit val ec               = context.system.dispatcher
  implicit val materializer     = Materializer(context)
  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")


  // Regulator File Scheduler Repos Annual
  def tsRepository2018                 = new TransmittalSheetRepository2018(dbConfig)
  def tsRepository2019                 = new TransmittalSheetRepository2019(dbConfig)
  def tsRepository2020                 = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Whole)
  def tsRepository2021                 = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Whole)

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

  //QA File Scheduler Repo Annual
  def qaTsRepository2018               = createPrivateQaTsRepository2018(dbConfig)
  def qaTsRepository2019               = createPrivateQaTsRepository2019(dbConfig)
  def qaTsRepository2020               = createQaTransmittalSheetRepository2020(dbConfig, Year2020Period.Whole)
  def qaTsRepository2021               = createQaTransmittalSheetRepository2021(dbConfig, Year2021Period.Whole)


  //QA File Scheduler Repo Quarterly
  def qaTsRepository2020Q1             = createQaTransmittalSheetRepository2020(dbConfig, Year2020Period.Q1)
  def qaTsRepository2020Q2             = createQaTransmittalSheetRepository2020(dbConfig, Year2020Period.Q2)
  def qaTsRepository2020Q3             = createQaTransmittalSheetRepository2020(dbConfig, Year2020Period.Q3)

  def qaTsRepository2021Q1             = createQaTransmittalSheetRepository2021(dbConfig, Year2021Period.Q1)
  def qaTsRepository2021Q2             = createQaTransmittalSheetRepository2021(dbConfig, Year2021Period.Q2)
  def qaTsRepository2021Q3             = createQaTransmittalSheetRepository2021(dbConfig, Year2021Period.Q3)

  def qaTsRepository2022Q1             = createQaTransmittalSheetRepository2022(dbConfig, Year2022Period.Q1)
  def qaTsRepository2022Q2             = createQaTransmittalSheetRepository2022(dbConfig, Year2022Period.Q2)
  def qaTsRepository2022Q3             = createQaTransmittalSheetRepository2022(dbConfig, Year2022Period.Q3)

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
      .schedule("TsSchedulerQuarterly2020", self, TsSchedulerQuarterly2020)

    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2021", self, TsSchedulerQuarterly2021)

    QuartzSchedulerExtension(context.system)
      .schedule("TsSchedulerQuarterly2022", self, TsSchedulerQuarterly2022)
  }

  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2019")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2020")
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler2021")

    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2020")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2021")
    QuartzSchedulerExtension(context.system).cancelJob("TsSchedulerQuarterly2022")

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

    case schedule @ TsScheduler2018 =>
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

        results.foreach(_ => persistFileForQa(fullFilePath, qaTsRepository2018))
        results.onComplete(reportPublishingResult(_, schedule, fullFilePath))
      }

    case schedule @ TsScheduler2019 =>
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

        results.foreach(_ => persistFileForQa(fullFilePath, qaTsRepository2019))
        results.onComplete(reportPublishingResult(_, schedule, fullFilePath))
      }

    case schedule @ TsScheduler2020 =>
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
          uploadFileToS3(s3Sink, tsRepository2020.getAllSheets(getFilterList()))

        results.foreach(_ => persistFileForQa(fullFilePath, qaTsRepository2020))
        results.onComplete(reportPublishingResult(_, schedule, fullFilePath))
      }
    case schedule @ TsScheduler2021 =>
      publishingGuard.runIfDataIsValid(Period.y2021, Scope.Private) {
        val now           = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName      = s"$formattedDate" + "2021_ts.txt"
        val s3Path        = s"$environmentPrivate/ts/"
        val fullFilePath  = SnapshotCheck.pathSelector(s3Path, fileName)

        val s3Sink =
          S3.multipartUpload(bucketPrivate, fullFilePath)
            .withAttributes(S3Attributes.settings(s3Settings))

        val results: Future[MultipartUploadResult] =
          uploadFileToS3(s3Sink, tsRepository2021.getAllSheets(getFilterList()))

        results.foreach(_ => persistFileForQa(fullFilePath, qaTsRepository2021))
        results.onComplete(reportPublishingResult(_, schedule, fullFilePath))
      }

    case schedule @ TsSchedulerQuarterly2020 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)
      val s3Path        = s"$environmentPrivate/ts/"
      def publishQuarter[Table <: RealTransmittalSheetTable2020](
                                                              quarter: Period.Quarter,
                                                              repo: TSRepository2020Base[Table],
                                                              fileNameSuffix: String,
                                                              qaRepository: QARepository[TransmittalSheetEntity]
                                                            ) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {
            val fileName     = formattedDate + fileNameSuffix
            val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)
            val s3Sink =
              S3.multipartUpload(bucketPrivate, fullFilePath)
                .withAttributes(S3Attributes.settings(s3Settings))

            def data: Future[Seq[TransmittalSheetEntity]] =
              repo.getAllSheets(getFilterList())

            val results: Future[MultipartUploadResult] =
              uploadFileToS3(s3Sink, data)

            results.foreach(_ => persistFileForQa(fullFilePath, qaRepository))
            results.onComplete(reportPublishingResult(_, schedule, fullFilePath))

          }
        }

      publishQuarter(Period.y2020Q1, tsRepository2020Q1, "quarter_1_2020_ts.txt", qaTsRepository2020Q1)
      publishQuarter(Period.y2020Q2, tsRepository2020Q2, "quarter_2_2020_ts.txt", qaTsRepository2020Q2)
      publishQuarter(Period.y2020Q3, tsRepository2020Q3, "quarter_3_2020_ts.txt", qaTsRepository2020Q3)

    case schedule @ TsSchedulerQuarterly2021 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)
      val s3Path        = s"$environmentPrivate/ts/"
      def publishQuarter[Table <: RealTransmittalSheetTable2021](
                                                                  quarter: Period.Quarter,
                                                                  repo: TSRepository2021Base[Table],
                                                                  fileNameSuffix: String,
                                                                  qaRepository: QARepository[TransmittalSheetEntity]
                                                                ) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {
            val fileName     = formattedDate + fileNameSuffix
            val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)
            val s3Sink =
              S3.multipartUpload(bucketPrivate, fullFilePath)
                .withAttributes(S3Attributes.settings(s3Settings))

            def data: Future[Seq[TransmittalSheetEntity]] =
              repo.getAllSheets(getFilterList())

            val results: Future[MultipartUploadResult] =
              uploadFileToS3(s3Sink, data)

            results.foreach(_ => persistFileForQa(fullFilePath, qaRepository))
            results.onComplete(reportPublishingResult(_, schedule, fullFilePath))

          }
        }

      publishQuarter(Period.y2021Q1, tsRepository2021Q1, "quarter_1_2021_ts.txt", qaTsRepository2021Q1)
      publishQuarter(Period.y2021Q2, tsRepository2021Q2, "quarter_2_2021_ts.txt", qaTsRepository2021Q2)
      publishQuarter(Period.y2021Q3, tsRepository2021Q3, "quarter_3_2021_ts.txt", qaTsRepository2021Q3)
    case schedule @ TsSchedulerQuarterly2022 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)
      val s3Path        = s"$environmentPrivate/ts/"
      def publishQuarter[Table <: RealTransmittalSheetTable2022](
                                                                  quarter: Period.Quarter,
                                                                  repo: TSRepository2022Base[Table],
                                                                  fileNameSuffix: String,
                                                                  qaRepository: QARepository[TransmittalSheetEntity]
                                                                ) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {
            val fileName     = formattedDate + fileNameSuffix
            val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)
            val s3Sink =
              S3.multipartUpload(bucketPrivate, fullFilePath)
                .withAttributes(S3Attributes.settings(s3Settings))

            def data: Future[Seq[TransmittalSheetEntity]] =
              repo.getAllSheets(getFilterList())

            val results: Future[MultipartUploadResult] =
              uploadFileToS3(s3Sink, data)

            results.foreach(_ => persistFileForQa(fullFilePath, qaRepository))
            results.onComplete(reportPublishingResult(_, schedule, fullFilePath))

          }
        }

      publishQuarter(Period.y2022Q1, tsRepository2022Q1, "quarter_1_2022_ts.txt", qaTsRepository2022Q1)
      publishQuarter(Period.y2022Q2, tsRepository2022Q2, "quarter_2_2022_ts.txt", qaTsRepository2022Q2)
      publishQuarter(Period.y2022Q3, tsRepository2022Q3, "quarter_3_2022_ts.txt", qaTsRepository2022Q3)
  }

  private def persistFileForQa(s3ObjKey: String, repository: QARepository[TransmittalSheetEntity]) = {
    val spec = QAFileSpec(
      bucket = bucketPrivate,
      key = s3ObjKey,
      s3Settings = s3Settings,
      withHeaderLine = false,
      parseLine = TransmittalSheetEntity.RegulatorParser.parseFromPSVUnsafe,
      repository = repository
    )
    qaFilePersistor.fetchAndPersist(spec)
  }

  def reportPublishingResult(result: Try[Any], schedule: Schedule, fullFilePath: String): Unit =
    result match {
      case Success(result) =>
        publishingReporter ! FilePublishingCompleted(
          schedule,
          fullFilePath,
          None,
          Instant.now,
          FilePublishingCompleted.Status.Success
        )
        log.info(s"Pushed to S3: $bucketPrivate/$fullFilePath.")
      case Failure(t) =>
        publishingReporter ! FilePublishingCompleted(
          schedule,
          fullFilePath,
          None,
          Instant.now,
          FilePublishingCompleted.Status.Error(t.getMessage)
        )
        log.error(s"An error has occurred while publishing $bucketPrivate/$fullFilePath: " + t.getMessage, t)
    }

}
// $COVERAGE-ON$