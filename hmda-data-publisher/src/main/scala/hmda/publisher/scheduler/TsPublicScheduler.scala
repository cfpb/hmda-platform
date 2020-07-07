package hmda.publisher.scheduler

import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.publisher.helper.{PublicAWSConfigLoader, SnapshotCheck, TSHeader}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019}
import hmda.publisher.scheduler.schedules.Schedules.{TsPublicScheduler2018, TsPublicScheduler2019}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsPublicScheduler extends HmdaActor with PublisherComponent2018 with PublisherComponent2019 with TSHeader with PublicAWSConfigLoader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)
  def tsRepository2018      = new TransmittalSheetRepository2018(dbConfig)
  def tsRepository2019      = new TransmittalSheetRepository2019(dbConfig)


  val s3Settings =
    S3Settings(context.system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProviderPublic)
      .withS3RegionProvider(awsRegionProviderPublic)
      .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsPublicScheduler2018", self, TsPublicScheduler2018)

    QuartzSchedulerExtension(context.system)
      .schedule("TsPublicScheduler2019", self, TsPublicScheduler2019)
  }

  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("TsPublicScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("TsPublicScheduler2019")
  }
  override def receive: Receive = {

    case TsPublicScheduler2018 =>
      val fileName      = "2018_ts.txt"
      val s3Path = s"$environmentPublic/dynamic-data/2018/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)
      tsPublicStream("2018", "cfpb-hmda-export", fullFilePath)

    case TsPublicScheduler2019 =>
      val fileName      = "2019_ts.txt"
      val s3Path = s"$environmentPublic/dynamic-data/2019/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)
      tsPublicStream("2019", "cfpb-hmda-export", fullFilePath)

  }
  private def tsPublicStream(year: String, bucket: String, path: String) = {

    val s3SinkPSV =

      S3.multipartUpload(bucket, path).withAttributes(S3Attributes.settings(s3Settings))

    val allResults: Future[Seq[TransmittalSheetEntity]] =
      year match {
        case "2018" =>        tsRepository2018.getAllSheets(getFilterList())
        case "2019" =>       tsRepository2019.getAllSheets(getFilterList())
        case _ => throw new IllegalArgumentException(s"Unknown year selector value:  [$year]")

      }
    //SYNC PSV
    val resultsPSV: Future[MultipartUploadResult] = Source
      .future(allResults)
      .mapConcat(seek => seek.toList)
      .zipWithIndex
      .map(transmittalSheet =>
        if (transmittalSheet._2 == 0) TSHeader.concat(transmittalSheet._1.toPublicPSV) + "\n"
        else transmittalSheet._1.toPublicPSV + "\n"
      )
      .map(s => ByteString(s))
      .runWith(s3SinkPSV)

    resultsPSV onComplete {
      case Success(result) =>
        log.info("Pushed to S3: " + path + ".")
      case Failure(t) =>
        log.info("An error has occurred with: " + path + "; Getting Public TS Data in Future: " + t.getMessage)
    }
  }
}