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
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.publisher.helper._
import hmda.publisher.qa.{QAFilePersistor, QAFileSpec, QARepository}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022}
import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.publisher.scheduler.schedules.Schedule
import hmda.publisher.scheduler.schedules.Schedules.{LarPublicScheduler2018, LarPublicScheduler2019, LarPublicScheduler2020}
import hmda.publisher.util.PublishingReporter
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{Period, Scope}
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import scala.util.{Failure, Success}
// $COVERAGE-OFF$
class LarPublicScheduler(publishingReporter: ActorRef[PublishingReporter.Command], qaFilePersistor: QAFilePersistor)
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with ModifiedLarHeader
    with PGTableNameLoader
    with PublicAWSConfigLoader
    with PrivateAWSConfigLoader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)

  def mlarRepository2018               = new ModifiedLarRepository2018(dbConfig)
  def qaMlarRepository2018               = new QAModifiedLarRepository2018(dbConfig)
  def mlarRepository2019               = new ModifiedLarRepository2019(dbConfig)
  def qaMlarRepository2019              = new QAModifiedLarRepository2019(dbConfig)
  def mlarRepository2020               = new ModifiedLarRepository2020(dbConfig)
  def qaMlarRepository2020              = new QAModifiedLarRepository2020(dbConfig)

  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPublic)
    .withS3RegionProvider(awsRegionProviderPublic)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    QuartzSchedulerExtension(context.system)
      .schedule("LarPublicScheduler2018", self, LarPublicScheduler2018)
    QuartzSchedulerExtension(context.system)
      .schedule("LarPublicScheduler2019", self, LarPublicScheduler2019)
    QuartzSchedulerExtension(context.system)
      .schedule("LarPublicScheduler2020", self, LarPublicScheduler2020)
  }
  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2019")
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2020")

  }
  override def receive: Receive = {
    case schedule @ LarPublicScheduler2018 =>
      publishingGuard.runIfDataIsValid(Period.y2018, Scope.Public) {
        val fileName         = "2018_lar.txt"
        val zipDirectoryName = "2018_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2018/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2018.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, schedule)
          //_ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2018)
        } yield ()


      }

    case schedule @ LarPublicScheduler2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Public) {
        val fileName         = "2019_lar.txt"
        val zipDirectoryName = "2019_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2019/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2019.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, schedule)
         // _ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2019)
        } yield ()
      }


    case schedule @ LarPublicScheduler2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Public) {
        val fileName         = "2020_lar.txt"
        val zipDirectoryName = "2020_lar.zip"
        val s3Path           = s"$environmentPublic/dynamic-data/2020/"
        val fullFilePath     = SnapshotCheck.pathSelector(s3Path, zipDirectoryName)
        val bucket           = if (SnapshotCheck.snapshotActive) SnapshotCheck.snapshotBucket else bucketPublic

        for {
          result <- larPublicStream(mlarRepository2020.getAllLARs(getFilterList()), bucket, fullFilePath, fileName, schedule)
          // _ <- persistFileForQa(result.key, result.bucket, ModifiedLarEntityImpl.parseFromPSVUnsafe, qaMlarRepository2020)
        } yield ()
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

  private def persistFileForQa[T](s3ObjKey: String, bucket: String, parseLine: String => T, repository: QARepository[T]) = {
    val spec = QAFileSpec(
      bucket = bucket,
      key = s3ObjKey,
      s3Settings = s3Settings,
      withHeaderLine = true,
      parseLine = parseLine,
      repository = repository
    )
    qaFilePersistor.fetchAndPersist(spec)
  }

}
// $COVERAGE-ON$