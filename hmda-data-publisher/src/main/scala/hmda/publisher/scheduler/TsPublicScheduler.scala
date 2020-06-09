package hmda.publisher.scheduler

import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.TSHeader
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019}
import hmda.publisher.scheduler.schedules.Schedules.{TsPublicScheduler2018, TsPublicScheduler2019}
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.util.BankFilterUtils._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsPublicScheduler extends HmdaActor with PublisherComponent2018 with PublisherComponent2019 with TSHeader {

  implicit val ec           = context.system.dispatcher
  implicit val materializer = Materializer(context)
  def tsRepository2018      = new TransmittalSheetRepository2018(dbConfig)
  def tsRepository2019      = new TransmittalSheetRepository2019(dbConfig)


  val awsConfig              = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId            = awsConfig.getString("public-access-key-id")
  val secretAccess           = awsConfig.getString("public-secret-access-key ")
  val region                 = awsConfig.getString("public-region")
  val bucket                 = awsConfig.getString("public-s3-bucket")
  val environment            = awsConfig.getString("public-environment")
  val year                   = awsConfig.getString("public-year")
  val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))

  val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

  val s3Settings =
    S3Settings(context.system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProvider)
      .withS3RegionProvider(awsRegionProvider)
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
      tsPublicStream("2018")

    case TsPublicScheduler2019 =>
      tsPublicStream("2019")

  }
  private def tsPublicStream(year: String) = {
    val fileNamePSV = year+"_ts.txt"

    val s3SinkPSV =
      S3.multipartUpload(bucket, s"$environment/dynamic-data/"+year+"/"+fileNamePSV)
        .withAttributes(S3Attributes.settings(s3Settings))

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
        log.info("Pushed to S3: " + s"$bucket/$environment/dynamic-data/"+year+"/"+fileNamePSV + ".")
      case Failure(t) =>
        println(" An error has occurred getting Public PSV TS Data "+year+": " + t.getMessage)
    }
  }
}