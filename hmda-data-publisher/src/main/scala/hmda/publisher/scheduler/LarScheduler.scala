package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.actor.HmdaActor
import hmda.census.records.CensusRecords
import hmda.model.census.Census
import hmda.model.publication.Msa
import hmda.publisher.helper.{LoanLimitLarHeader, PrivateAWSConfigLoader, SnapshotCheck}
import hmda.publisher.query.component.{PublisherComponent2018, PublisherComponent2019, PublisherComponent2020}
import hmda.publisher.query.lar.{LarEntityImpl2018, LarEntityImpl2019, LarEntityImpl2020}
import hmda.publisher.scheduler.schedules.Schedules.{LarScheduler2018, LarScheduler2019, LarSchedulerLoanLimit2019, LarSchedulerQuarterly2020}
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import slick.basic.DatabasePublisher
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.Future
import scala.util.{Failure, Success}

class LarScheduler
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with LoanLimitLarHeader
    with PrivateAWSConfigLoader {


  implicit val ec               = context.system.dispatcher
  implicit val materializer     = Materializer(context)
  private val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  def larRepository2018 = new LarRepository2018(dbConfig)
  def larRepository2019 = new LarRepository2019(dbConfig)
  def larRepository2020 = new LarRepository2020(dbConfig)

  val indexTractMap2018: Map[String, Census] = CensusRecords.indexedTract2018
  val indexTractMap2019: Map[String, Census] = CensusRecords.indexedTract2019

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPrivate)
    .withS3RegionProvider(awsRegionProviderPrivate)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler2018", self, LarScheduler2018)
    QuartzSchedulerExtension(context.system)
      .schedule("LarScheduler2019", self, LarScheduler2019)
    QuartzSchedulerExtension(context.system)
      .schedule("LarSchedulerLoanLimit2019", self, LarSchedulerLoanLimit2019)
    QuartzSchedulerExtension(context.system)
      .schedule("LarSchedulerQuarterly2020", self, LarSchedulerQuarterly2020)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("LarScheduler2019")
    QuartzSchedulerExtension(context.system).cancelJob("LarSchedulerLoanLimit2019")
    QuartzSchedulerExtension(context.system).cancelJob("LarSchedulerQuarterly2020")
  }

  override def receive: Receive = {

    case LarScheduler2018 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName = s"$formattedDate" + "2018_lar.txt"
      val s3Path = s"$environmentPrivate/lar/"
      val fullFilePath= SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink = S3
        .multipartUpload(bucketPrivate,fullFilePath )
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl2018] =
        larRepository2018.getAllLARs(getFilterList())
      val allResultsSource: Source[LarEntityImpl2018, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      val results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toRegulatorPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")
        case Failure(t) =>
          log.info("An error has occurred getting LAR Data in Future: " + t.getMessage)
      }

    case LarScheduler2019 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName = s"$formattedDate" + "2019_lar.txt"
      val s3Path = s"$environmentPrivate/lar/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)
      val s3Sink = S3
        .multipartUpload(bucketPrivate, fullFilePath)
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl2019] =
        larRepository2019.getAllLARs(getFilterList())
      val allResultsSource: Source[LarEntityImpl2019, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      val results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toRegulatorPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")
        case Failure(t) =>
          log.info("An error has occurred getting LAR Data 2019 in Future: " + t.getMessage)
      }

    case LarSchedulerLoanLimit2019 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName      = "2019F_AGY_LAR_withFlag_" + s"$formattedDate" + "2019_lar.txt"
      val s3Path = s"$environmentPrivate/lar/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink = S3
        .multipartUpload(bucketPrivate, fullFilePath)
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl2019] =
        larRepository2019.getAllLARs(getFilterList())
      val allResultsSource: Source[LarEntityImpl2019, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      val resultsPSV: Future[MultipartUploadResult] =
        allResultsSource.zipWithIndex
          .map(
            larEntity =>
              if (larEntity._2 == 0)
                LoanLimitHeader.concat(appendCensus(larEntity._1,2019) ) + "\n"
              else appendCensus(larEntity._1,2019) + "\n")
          .map(s => ByteString(s))
          .runWith(s3Sink)

      resultsPSV onComplete {
        case Success(results) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")
        case Failure(t) =>
          log.info("An error has occurred getting LAR Data Loan Limit2019 in Future: " + t.getMessage)
      }

    case LarSchedulerQuarterly2020 =>
      val includeQuarterly = true
      val now              = LocalDateTime.now().minusDays(1)
      val formattedDate    = fullDateQuarterly.format(now)

      val fileName = s"$formattedDate" + "quarterly_2020_lar.txt"

      val s3Path = s"$environmentPrivate/lar/"
      val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

      val s3Sink = S3
        .multipartUpload(bucketPrivate, fullFilePath)
        .withAttributes(S3Attributes.settings(s3Settings))

      val allResultsPublisher: DatabasePublisher[LarEntityImpl2020] =
        larRepository2020.getAllLARs(getFilterList(), includeQuarterly)
      val allResultsSource: Source[LarEntityImpl2020, NotUsed] =
        Source.fromPublisher(allResultsPublisher)

      val results: Future[MultipartUploadResult] = allResultsSource
        .map(larEntity => larEntity.toRegulatorPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) =>
          log.info("Pushed to S3: " +  s"$bucketPrivate/$fullFilePath"  + ".")
        case Failure(t) =>
          log.info("An error has occurred getting Quarterly LAR Data 2020 in Future: " + t.getMessage)
      }
  }

  def getCensus(hmdaGeoTract: String,year:Int): Msa = {

    val  indexTractMap = year match {
      case 2018 => indexTractMap2018
      case 2019 => indexTractMap2019
      case _ => indexTractMap2019
    }
    val censusResult= indexTractMap.getOrElse(hmdaGeoTract, Census())
    val censusID =
      if (censusResult.msaMd == 0) "-----" else censusResult.msaMd.toString
    val censusName =
      if (censusResult.name.isEmpty) "MSA/MD NOT AVAILABLE" else censusResult.name
    Msa(censusID, censusName)
  }

  def appendCensus(lar: LarEntityImpl2019,year:Int): String = {
    val msa = getCensus(lar.larPartOne.tract,year)
    lar.appendMsa(msa)
  }
}