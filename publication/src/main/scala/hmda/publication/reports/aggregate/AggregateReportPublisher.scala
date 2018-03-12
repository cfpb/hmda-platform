package hmda.publication.reports.aggregate

import java.util.concurrent.CompletionStage

import akka.NotUsed
import akka.actor.{ ActorSystem, Props }
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.Supervision._
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.{ ByteString, Timeout }
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import hmda.persistence.model.HmdaActor
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository
import akka.stream.alpakka.s3.javadsl.MultipartUploadResult
import hmda.census.model.MsaIncomeLookup
import hmda.persistence.messages.commands.publication.PublicationCommands.GenerateAggregateReports

import scala.concurrent.Future
import scala.concurrent.duration._

object AggregateReportPublisher {
  val name = "aggregate-report-publisher"
  def props(): Props = Props(new AggregateReportPublisher)
}

class AggregateReportPublisher extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

  val duration = config.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(duration.seconds)

  val accessKeyId = config.getString("hmda.publication.aws.access-key-id")
  val secretAccess = config.getString("hmda.publication.aws.secret-access-key ")
  val region = config.getString("hmda.publication.aws.region")
  val bucket = config.getString("hmda.publication.aws.public-bucket")
  val environment = config.getString("hmda.publication.aws.environment")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  val aggregateReports: List[AggregateReport] = List(
    A42, A45, A46
  //A52, A53  TODO: fix these A5X reports, which cause timeout errors in the cluster
  )

  val nationalAggregateReports: List[AggregateReport] = List(
    N45, N46
  )

  override def receive: Receive = {

    case GenerateAggregateReports() =>
      log.info(s"Generating aggregate reports for 2017 filing year")
      generateReports

    case _ => //do nothing
  }

  private def generateReports = {
    val msaList = MsaIncomeLookup.everyFips.toList
    val parallelism = 1

    val combinations = combine(msaList, aggregateReports) ++ combine(List(-1), nationalAggregateReports)

    val reportS3Flow: Flow[(Int, AggregateReport), CompletionStage[MultipartUploadResult], NotUsed] =
      Flow[(Int, AggregateReport)].mapAsync(parallelism) {
        case (msa, report) => publishSingleReport(msa, report)
      }

    Source(combinations).via(reportS3Flow).runWith(Sink.ignore)
  }

  private def publishSingleReport(msa: Int, report: AggregateReport): Future[CompletionStage[MultipartUploadResult]] = {
    val larSource = readData(1000)
    report.generate(larSource, msa).map { payload =>
      val filePath = s"$environment/reports/aggregate/2017/${payload.msa}/${payload.reportID}.txt"
      log.info(s"Publishing Aggregate report. MSA: ${payload.msa}, Report #: ${payload.reportID}")

      Source.single(ByteString(payload.report))
        .runWith(s3Client.multipartUpload(bucket, filePath))
    }
  }

  /**
   * Returns all combinations of MSA and Aggregate Reports
   * Input:   List(407, 508) and List(A41, A42)
   * Returns: List((407, A41), (407, A42), (508, A41), (508, A42))
   */
  private def combine(msas: List[Int], reports: List[AggregateReport]): List[(Int, AggregateReport)] = {
    msas.flatMap(msa => List.fill(reports.length)(msa).zip(reports))
  }

}

