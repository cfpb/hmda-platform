package hmda.publisher.scheduler

import java.time.Instant
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
import hmda.publisher.helper._
import hmda.publisher.query.component.{ ModifiedLarRepository, PublisherComponent, PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023 }
import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear }
import hmda.publisher.scheduler.schedules.Schedules.{ LarPublicSchedule, LarPublicScheduler2018, LarPublicScheduler2019, LarPublicScheduler2020, LarPublicScheduler2021 }
import hmda.publisher.util.{ PublishingReporter, ScheduleCoordinator }
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{ Period, Scope }
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import scala.util.{ Failure, Success }
// $COVERAGE-OFF$
class LarPublicScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
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

  def mlarRepository2018               = new ModifiedLarRepository2018(dbConfig)
  def mlarRepository2019               = new ModifiedLarRepository2019(dbConfig)
  def mlarRepository2020               = new ModifiedLarRepository2020(dbConfig)
  def mlarRepository2021               = new ModifiedLarRepository2021(dbConfig)

  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPublic)
    .withS3RegionProvider(awsRegionProviderPublic)
    .withListBucketApiVersion(ListBucketVersion2)

  private val cronExpression = dynamicQuartzScheduleConfig.getString("LarPublicSchedule")

  override def preStart(): Unit = {
    availableRepos.foreach {
      case (year, _) => scheduler ! Schedule(s"LarPublicSchedule_$year", self, ScheduleWithYear(LarPublicSchedule, year), cronExpression)
    }
  }
  override def postStop(): Unit = {
    availableRepos.foreach {
      case (year, _) => scheduler ! Unschedule(s"LarPublicSchedule_$year")
    }


  }
  override def receive: Receive = {
    case LarPublicScheduler2018 =>
      publishingGuard.runIfDataIsValid(Period.y2018, Scope.Public) {
        val fileName         = "2018_lar.txt"
        val zipDirectoryName = "2018_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2018/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2018.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, LarPublicScheduler2018)
          //_ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2018)
        } yield ()


      }

    case LarPublicScheduler2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Public) {
        val fileName         = "2019_lar.txt"
        val zipDirectoryName = "2019_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2019/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2019.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, LarPublicScheduler2019)
         // _ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2019)
        } yield ()
      }


    case LarPublicScheduler2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Public) {
        val fileName         = "2020_lar.txt"
        val zipDirectoryName = "2020_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2020/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2020.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, LarPublicScheduler2020)
          // _ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2020)
        } yield ()
      }

    case LarPublicScheduler2021 =>
      publishingGuard.runIfDataIsValid(Period.y2021, Scope.Public) {
        val fileName         = "2021_lar.txt"
        val zipDirectoryName = "2021_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2021/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2021.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, LarPublicScheduler2021)
          // _ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2021)
        } yield ()
      }

    case ScheduleWithYear(schedule, year) if schedule == LarPublicSchedule =>
      publishingGuard.runIfDataIsValid(year, Scope.Public) {
        val fileName = s"${year}_lar.txt"
        val zipDirectoryName = s"${year}_lar.zip"
        val s3Path = s"$environmentPublic/dynamic-data/$year/"
        val fullFilePath = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        availableRepos(year) match {
          case repo =>
            for {
              _ <- larPublicStream(repo.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, LarPublicSchedule)
            } yield ()
          case _ => log.error("No available publisher found for {} in year {}", schedule, year)
        }
      }
  }

  private def larPublicStream(
                               data: DatabasePublisher[ModifiedLarEntityImpl],
                               bucket: String,
                               key: String,
                               fileName: String,
                               schedule: Schedule
                             ): Future[MultipartUploadResult] = {

    //PSV Sync
    val s3SinkPSV = S3
      .multipartUpload(bucket, key)
      .withAttributes(S3Attributes.settings(s3Settings))

    val fileStream: Source[ByteString, Any] =
      Source.fromPublisher(data)
        .map(_.toPublicPSV + "\n")
        .prepend(Source.single(MLARHeader))
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
        log.info("An error has occurred with: " + key + "; Getting Public LAR Data in Future: " + t.getMessage)
    }
    resultsPSV
  }


}
// $COVERAGE-ON$