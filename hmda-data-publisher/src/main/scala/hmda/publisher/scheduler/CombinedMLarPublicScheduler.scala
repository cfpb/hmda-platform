package hmda.publisher.scheduler

import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.alpakka.file.ArchiveMetadata
import akka.stream.alpakka.file.scaladsl.Archive
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.actor.HmdaActor
import hmda.publisher.helper.CronConfigLoader.{CronString, combinedMlarCron, combinedMlarYears}
import hmda.publisher.helper._
import hmda.publisher.query.component._
import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.publisher.scheduler.schedules.Schedules._
import hmda.publisher.scheduler.schedules.{Schedule, ScheduleWithYear}
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
import hmda.publisher.util.{PublishingReporter, ScheduleCoordinator}
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher

import java.time.Instant
import scala.concurrent.Future
import scala.concurrent.duration.HOURS
import scala.util.{Failure, Success}

// $COVERAGE-OFF$
class CombinedMLarPublicScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PublisherComponent2023
    with ModifiedLarHeader
    with PGTableNameLoader
    with PublicAWSConfigLoader
    with PrivateAWSConfigLoader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)

  val availableRepos: Map[Int, ModifiedLarRepository] = mLarAvailableYears.map(yr => yr -> {
    val component = new PublisherComponent(yr)
    new ModifiedLarRepository(dbConfig, component.mlarTable)
  }).toMap

  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPublic)
    .withS3RegionProvider(awsRegionProviderPublic)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    combinedMlarYears.zipWithIndex.foreach {
      case (year, idx) => scheduler ! Schedule(s"CombinedMLarPublicSchedule_$year", self, ScheduleWithYear(CombinedMLarPublicSchedule, year), combinedMlarCron.applyOffset(idx, HOURS))
    }
  }
  override def postStop(): Unit = {
    combinedMlarYears.foreach(year => scheduler ! Unschedule(s"CombinedMLarPublicScheduler_$year"))
  }
  override def receive: Receive = {

    case ScheduleWithYear(schedule, year) if schedule == CombinedMLarPublicSchedule =>
      publishingGuard.runIfDataIsValid(year, YearPeriod.Whole, Scope.Public) {

        val fileNameHeader = s"${year}_combined_mlar_header.txt"
        val zipNameHeader = s"${year}_combined_mlar_header.zip"
        val s3PathHeader = s"$environmentPublic/dynamic-data/combined-mlar/$year/header/"
        val fullFilePathHeader     = SnapshotCheck.pathSelector(s3PathHeader, zipNameHeader)

        val fileName = s"${year}_combined_mlar.txt"
        val zipFileName = s"${year}_combined_mlar.zip"
        val s3Path = s"$environmentPublic/dynamic-data/combined-mlar/$year/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipFileName)


        availableRepos.get(year) match {
          case Some(repo) =>
            for {
              _ <- larPublicStream(repo.getAllLARs(getFilterList()), bucketPublic, fullFilePathHeader, fileNameHeader, CombinedMLarPublicSchedule,"header")
              _ <- larPublicStream(repo.getAllLARs(getFilterList()), bucketPublic, fullFilePath, fileName, CombinedMLarPublicSchedule,"")
            } yield ()
          case None => log.error("No available publisher found for {} in year {}", schedule, year)
        }
      }
  }

  private def larPublicStream(
                               data: DatabasePublisher[ModifiedLarEntityImpl],
                               bucket: String,
                               key: String,
                               fileName: String,
                               schedule: Schedule,
                               fileType: String
                             ): Future[MultipartUploadResult] = {

    //PSV Sync
    val s3SinkPSV = S3
      .multipartUpload(bucket, key)
      .withAttributes(S3Attributes.settings(s3Settings))

    val fileStream: Source[ByteString, Any] =
      Source.fromPublisher(data)
        .map(_.toCombinedMLAR("|") + "\n")
        .prepend(Source.single(if (fileType.equalsIgnoreCase("header"))CombinedMLARHeaderPSV else ""))
        .map(s => ByteString(s))

    val zipStream = Source(List((ArchiveMetadata(fileName), fileStream)))

    val resultsPSV = for {
      _            <- S3Archiver.archiveFileIfExists(bucket, key, bucketPrivate, s3Settings)
      source       = zipStream.via(Archive.zip())
      uploadResult <- S3Utils.uploadWithRetry(source, s3SinkPSV)
    } yield uploadResult

    def sendPublishingNotif(error: Option[String]): Unit = {
      val status = error match {
        case Some(value) => FilePublishingCompleted.Status.Error(value)
        case None        => FilePublishingCompleted.Status.Success
      }
      publishingReporter ! FilePublishingCompleted(schedule, key, None, Instant.now(), status)
    }

    resultsPSV onComplete {
      case Success(result) =>
        sendPublishingNotif(None)
        log.info("Pushed to S3: " + s"$bucket/$key" + ".")
      case Failure(t) =>
        sendPublishingNotif(Some(t.getMessage))
        log.info("An error has occurred with: " + key + "; Getting Public MLAR Data in Future: " + t)
    }
    resultsPSV
  }


}
// $COVERAGE-ON$