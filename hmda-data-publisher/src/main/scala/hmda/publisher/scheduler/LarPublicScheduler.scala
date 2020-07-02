package hmda.publisher.scheduler

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.publisher.helper.{ModifiedLarHeader, PGTableNameLoader, PublicAWSConfigLoader, SnapshotCheck}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019}
import hmda.publisher.query.lar.ModifiedLarEntityImpl
import hmda.publisher.scheduler.schedules.Schedules.{LarPublicScheduler2018, LarPublicScheduler2019}
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LarPublicScheduler extends HmdaActor with
  PublisherComponent2018 with
  PublisherComponent2019 with
  ModifiedLarHeader with
  PGTableNameLoader with
  PublicAWSConfigLoader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)

  def mlarRepository2018 = new ModifiedLarRepository2018(dbConfig)
  def mlarRepository2019 = new ModifiedLarRepository2019(dbConfig)

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
  }
  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("LarPublicScheduler2019")
  }
  override def receive: Receive = {

    case LarPublicScheduler2018 =>
      if (SnapshotCheck.snapshotActive) {
        val s3Path = "dev/snapshot-temp/2018/2018_lar_snapshot.txt"
        larPublicStream("2018", "cfpb-hmda-export", s3Path)
      }
      else{
        val s3Path = s"$environmentPublic/dynamic-data/2018/2018_lar.txt"
        larPublicStream("2018", bucketPublic, s3Path)
      }

    case LarPublicScheduler2019 =>
      if (SnapshotCheck.snapshotActive) {
         val s3Path = "dev/snapshot-temp/2019/2019_lar_snapshot.txt"
        larPublicStream("2019", "cfpb-hmda-export", s3Path)
      }
      else{
        val s3Path = s"$environmentPublic/dynamic-data/2019/2019_lar.txt"
        larPublicStream("2019", bucketPublic, s3Path)
      }
  }

  private def larPublicStream(year: String, bucket: String, path: String) = {

    val allResultsPublisher: DatabasePublisher[ModifiedLarEntityImpl] =
      year match {
        case "2018" =>        mlarRepository2018.getAllLARs(getFilterList())
        case "2019" =>       mlarRepository2019.getAllLARs(getFilterList())
        case _ => throw new IllegalArgumentException(s"Unknown year selector value:  [$year]")
      }

    val allResultsSource: Source[ModifiedLarEntityImpl, NotUsed] =
      Source.fromPublisher(allResultsPublisher)

    //PSV Sync
    val s3SinkPSV = S3
      .multipartUpload(bucket, path)
      .withAttributes(S3Attributes.settings(s3Settings))

    val resultsPSV: Future[MultipartUploadResult] =
      allResultsSource.zipWithIndex
        .map(mlarEntity =>
          if (mlarEntity._2 == 0)
            MLARHeader.concat(mlarEntity._1.toPublicPSV) + "\n"
          else mlarEntity._1.toPublicPSV + "\n"
        )
        .map(s => ByteString(s))
        .runWith(s3SinkPSV)

    resultsPSV onComplete {
      case Success(result) =>
        log.info("Pushed to S3: " + path + ".")
      case Failure(t) =>
        log.info("An error has occurred with: " + path + "; Getting Public LAR Data in Future: " + t.getMessage)
    }
  }
}