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
import hmda.publisher.scheduler.schedules.Schedules.{ TsPublicSchedule }
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._
import akka.stream.alpakka.file.scaladsl.Archive
import akka.stream.alpakka.file.ArchiveMetadata
import hmda.publisher.helper.CronConfigLoader.{ CronString, tsPublicCron, tsPublicYears }
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear }
import hmda.publisher.util.{ PublishingReporter, ScheduleCoordinator }
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{ Scope }

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import java.time.Instant
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._

import scala.concurrent.duration.HOURS
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
    tsPublicYears.zipWithIndex.foreach {
      case (year, idx) => scheduler ! Schedule(s"TsPublicSchedule_$year", self, ScheduleWithYear(TsPublicSchedule, year), tsPublicCron.applyOffset(idx, HOURS))
    }
  }

  override def postStop(): Unit = {
    tsPublicYears.foreach(year => scheduler ! Unschedule(s"TsPublicSchedule_$year"))
  }
  override def receive: Receive = {

    case ScheduleWithYear(schedule, year) if schedule == TsPublicSchedule =>
      publishingGuard.runIfDataIsValid(year, YearPeriod.Whole, Scope.Public) {
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

}
// $COVERAGE-ON$