package hmda.publisher.scheduler

import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.actor.HmdaActor
import hmda.publisher.helper.{ PrivateAWSConfigLoader, PublicAWSConfigLoader, S3Archiver, S3Utils, SnapshotCheck, TSHeader }
import hmda.publisher.query.component.{ PublisherComponent, PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023, TransmittalSheetTable, TsRepository, YearPeriod }
import hmda.publisher.scheduler.schedules.Schedules.{ TsPublicSchedule, TsPublicScheduler2018, TsPublicScheduler2019, TsPublicScheduler2020, TsPublicScheduler2021 }
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._
import akka.stream.alpakka.file.scaladsl.Archive
import akka.stream.alpakka.file.ArchiveMetadata
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear }
import hmda.publisher.util.{ PublishingReporter, ScheduleCoordinator }
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{ Period, Scope }

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import java.time.Instant
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
// $COVERAGE-OFF$
class TsPublicScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PublisherComponent2023
    with TSHeader
    with PublicAWSConfigLoader
    with PrivateAWSConfigLoader {

  implicit val ec                      = context.system.dispatcher
  implicit val materializer            = Materializer(context)
  private val tsPublicCronExpression = dynamicQuartzScheduleConfig.getString("TsPublicSchedule")
  def tsRepository2018                 = new TsRepository[TransmittalSheetTable](dbConfig, transmittalSheetTable2018)
  def tsRepository2019                 = new TsRepository[TransmittalSheetTable](dbConfig, transmittalSheetTable2019)
  def tsRepository2020                 = createTransmittalSheetRepository2020(dbConfig, Year2020Period.Whole)
  def tsRepository2021                 = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Whole)

  val availableRepos = tsAvailableYears.map(year => year -> {
    val component = new PublisherComponent(year)
    new TsRepository[TransmittalSheetTable](dbConfig, component.transmittalSheetTableQuery(YearPeriod.Whole))
  }).toMap


  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)


  val s3Settings =
    S3Settings(context.system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProviderPublic)
      .withS3RegionProvider(awsRegionProviderPublic)
      .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    availableRepos.foreach {
      case (year, _) => scheduler ! Schedule(s"TsPublicSchedule_$year", self, ScheduleWithYear(TsPublicSchedule, year), tsPublicCronExpression)
    }
  }

  override def postStop(): Unit = {
    availableRepos.foreach {
      case (year, _) => scheduler ! Unschedule(s"TsPublicSchedule_$year")
    }
  }
  override def receive: Receive = {

    case TsPublicScheduler2018 =>
      publishingGuard.runIfDataIsValid(Period.y2018, Scope.Public) {
        val fileName         = "2018_ts.txt"
        val zipDirectoryName = "2018_ts.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2018/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        val result = tsPublicStream("2018", bucket, fullFilePath, fileName, TsPublicScheduler2018)
        //result.foreach(r => persistFileForQa(r.key, r.bucket, qaRepo2018))
      }

    case TsPublicScheduler2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Public) {
        val fileName         = "2019_ts.txt"
        val zipDirectoryName = "2019_ts.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2019/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        val result = tsPublicStream("2019", bucket, fullFilePath, fileName, TsPublicScheduler2019)
        //result.foreach(r => persistFileForQa(r.key, r.bucket, qaRepo2019))
      }

    case TsPublicScheduler2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Public) {
        val fileName         = "2020_ts.txt"
        val zipDirectoryName = "2020_ts.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2020/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        val result = tsPublicStream("2020", bucket, fullFilePath, fileName, TsPublicScheduler2020)
        //result.foreach(r => persistFileForQa(r.key, r.bucket, qaRepo2020))
      }

    case TsPublicScheduler2021 =>
      publishingGuard.runIfDataIsValid(Period.y2021, Scope.Public) {
        val fileName         = "2021_ts.txt"
        val zipDirectoryName = "2021_ts.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2021/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        val result = tsPublicStream("2021", bucket, fullFilePath, fileName, TsPublicScheduler2021)
        //result.foreach(r => persistFileForQa(r.key, r.bucket, qaRepo2020))
      }

    case ScheduleWithYear(schedule, year) if schedule == TsPublicSchedule =>
      publishingGuard.runIfDataIsValid(year, Scope.Public) {
        val fileName = s"${year}_ts.txt"
        val zipDirectoryName = s"${year}_ts.zip"
        val s3Path = s"$environmentPublic/dynamic-data/$year/"
        val fullFilePath = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        val result = tsPublicStream(year, bucket, fullFilePath, fileName, TsPublicSchedule)
        //result.foreach(r => persistFileForQa(r.key, r.bucket, qaRepo2020))
      }
  }

  private def tsPublicStream(
    year: Int,
    bucket: String,
    key: String,
    fileName: String,
    schedule: Schedule
  ): Future[MultipartUploadResult] = availableRepos.get(year) match {
    case Some(repo) =>
      val s3SinkPSV = S3.multipartUpload(bucket, key).withAttributes(S3Attributes.settings(s3Settings))
      val allResults: Future[Seq[TransmittalSheetEntity]] = repo.getAllSheets(getFilterList())
      //SYNC PSV
      val fileStream: Source[ByteString, Any] = Source
        .future(allResults)
        .mapConcat(seek => seek.toList)
        .map(_.toPublicPSV + "\n")
        .prepend(Source.single(TSHeader))
        .map(s => ByteString(s))

      val zipStream = Source(
        List((ArchiveMetadata(fileName), fileStream))
      )

      val resultsPSV = for {
        _ <- S3Archiver.archiveFileIfExists(bucket, key, bucketPrivate, s3Settings)
        bytesStream = zipStream.via(Archive.zip())
        uploadResult <- S3Utils.uploadWithRetry(bytesStream, s3SinkPSV)
      } yield uploadResult

      resultsPSV onComplete {
        case Success(result) =>
          publishingReporter ! FilePublishingCompleted(schedule, key, None, Instant.now, FilePublishingCompleted.Status.Success)
          log.info("Pushed to S3: " + s"$bucket/$key" + ".")
        case Failure(t) =>
          publishingReporter ! FilePublishingCompleted(schedule, key, None, Instant.now, FilePublishingCompleted.Status.Error(t.getMessage))
          log.info("An error has occurred with: " + key + "; Getting Public TS Data in Future: " + t.getMessage)
      }
      resultsPSV
    case None => throw new IllegalArgumentException(s"Unknown year selector value:  [$year]")
  }

  private def tsPublicStream(
                              year: String,
                              bucket: String,
                              key: String,
                              fileName: String,
                              schedule: Schedule
                            ): Future[MultipartUploadResult] = {

    val s3SinkPSV =
      S3.multipartUpload(bucket, key).withAttributes(S3Attributes.settings(s3Settings))

    val allResults: Future[Seq[TransmittalSheetEntity]] =
      year match {
        case "2018" => tsRepository2018.getAllSheets(getFilterList())
        case "2019" => tsRepository2019.getAllSheets(getFilterList())
        case "2020" => tsRepository2020.getAllSheets(getFilterList())
        case "2021" => tsRepository2021.getAllSheets(getFilterList())
        case _      => throw new IllegalArgumentException(s"Unknown year selector value:  [$year]")

      }
    //SYNC PSV
    val fileStream: Source[ByteString, Any] = Source
      .future(allResults)
      .mapConcat(seek => seek.toList)
      .map(_.toPublicPSV + "\n")
      .prepend(Source.single(TSHeader))
      .map(s => ByteString(s))

    val zipStream = Source(
      List((ArchiveMetadata(fileName), fileStream))
    )

    val resultsPSV = for {
      _            <- S3Archiver.archiveFileIfExists(bucket, key, bucketPrivate, s3Settings)
      bytesStream  = zipStream.via(Archive.zip())
      uploadResult <- S3Utils.uploadWithRetry(bytesStream, s3SinkPSV)
    } yield uploadResult

    resultsPSV onComplete {
      case Success(result) =>
        publishingReporter ! FilePublishingCompleted(schedule, key, None, Instant.now, FilePublishingCompleted.Status.Success)
        log.info("Pushed to S3: " + s"$bucket/$key" + ".")
      case Failure(t) =>
        publishingReporter ! FilePublishingCompleted(schedule, key, None, Instant.now, FilePublishingCompleted.Status.Error(t.getMessage))
        log.info("An error has occurred with: " + key + "; Getting Public TS Data in Future: " + t.getMessage)
    }
    resultsPSV
  }

}
// $COVERAGE-ON$