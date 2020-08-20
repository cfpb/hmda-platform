package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, MetaHeaders, MultipartUploadResult, S3Attributes, S3Settings}
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

      val allResultsSource: Source[String, NotUsed] =
        Source.fromPublisher(larRepository2019.getAllLARs(getFilterList()))
          .map(larEntity => larEntity.toRegulatorPSV)
      def countF: Future[Int] = larRepository2019.getAllLARsCount(getFilterList())

      publishPSVtoS3(fileName, allResultsSource, countF)

    case LarScheduler2019 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName = s"$formattedDate" + "2019_lar.txt"

      val allResultsSource: Source[String, NotUsed] =
        Source.fromPublisher(larRepository2019.getAllLARs(getFilterList()))
          .map(larEntity => appendCensus(larEntity, 2019))
          .prepend(Source.single(LoanLimitHeader))
      def countF: Future[Int] = larRepository2019.getAllLARsCount(getFilterList())

      publishPSVtoS3(fileName, allResultsSource, countF)

    case LarSchedulerLoanLimit2019 =>
      val now           = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDate.format(now)
      val fileName      = "2019F_AGY_LAR_withFlag_" + s"$formattedDate" + "2019_lar.txt"

      val allResultsSource: Source[String, NotUsed] =
        Source.fromPublisher(larRepository2019.getAllLARs(getFilterList()))
          .map(larEntity => appendCensus(larEntity, 2019))
          .prepend(Source.single(LoanLimitHeader))
      def countF: Future[Int] = larRepository2019.getAllLARsCount(getFilterList())

      publishPSVtoS3(fileName, allResultsSource, countF)

    case LarSchedulerQuarterly2020 =>
      val includeQuarterly = true
      val now              = LocalDateTime.now().minusDays(1)
      val formattedDate    = fullDateQuarterly.format(now)
      val fileName = s"$formattedDate" + "quarterly_2020_lar.txt"

      val allResultsSource: Source[String, NotUsed] =
        Source
          .fromPublisher(larRepository2020.getAllLARs(getFilterList(), includeQuarterly))
          .map(larEntity => larEntity.toRegulatorPSV)
      def countF: Future[Int] = larRepository2020.getAllLARsCount(getFilterList(), includeQuarterly)

      publishPSVtoS3(fileName, allResultsSource, countF)
  }

  def publishPSVtoS3(fileName: String, rows: Source[String, NotUsed], countF: => Future[Int]): Unit = {
    val s3Path       = s"$environmentPrivate/lar/"
    val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)

    val bytesStream: Source[ByteString, NotUsed] =
      rows
        .map(_ + "\n")
        .map(s => ByteString(s))

    val results = for {
      count <- countF
      s3Sink = S3
        .multipartUpload(bucketPrivate, fullFilePath, metaHeaders = MetaHeaders(Map(LarScheduler.entriesCountMetaName -> count.toString)))
        .withAttributes(S3Attributes.settings(s3Settings))
      result <- bytesStream.runWith(s3Sink)
    } yield result

    results onComplete {
      case Success(result) =>
        log.info(s"Pushed to S3: $bucketPrivate/$fullFilePath.")
      case Failure(t) =>
        log.info(s"An error has occurred pushing LAR Data to $bucketPrivate/$fullFilePath: ${t.getMessage}")
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

object LarScheduler{
  val entriesCountMetaName = "entries-count"
}