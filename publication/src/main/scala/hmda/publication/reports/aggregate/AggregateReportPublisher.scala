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
import hmda.publication.reports

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
    AggregateA1, AggregateA2, AggregateA3,
    AggregateA4,
    AggregateB,
    A42, A43, A45, A46, A47,
    A51, A52, A53, A54, A56, A57,
    A71, A72, A73, A74, A75, A76, A77,
    A81, A82, A83, A84, A85, A86, A87,
    A11_1, A11_2, A11_3, A11_4, A11_5, A11_6, A11_7, A11_8, A11_9, A11_10,
    A12_1, A12_2
  )

  val nationalAggregateReports: List[AggregateReport] = List(
    NationalAggregateA1, NationalAggregateA2, NationalAggregateA3,
    NationalAggregateA4,
    NationalAggregateB,
    N41, N43, N45, N46, N47,
    N51, N52, N53, N54, N56, N57,
    N71, N72, N73, N74, N75, N76, N77,
    N81, N82, N83, N84, N85, N86, N87,
    N11_1, N11_2, N11_3, N11_4, N11_5, N11_6, N11_7, N11_8, N11_9, N11_10,
    N12_1, N12_2
  )

  override def receive: Receive = {

    case GenerateAggregateReports() =>
      log.info(s"Generating aggregate reports for 2017 filing year")
      generateReports

    case _ => //do nothing
  }

  private def generateReports = {
    val larSource = readData(1000)
    val msaList = MsaIncomeLookup.everyFips.toList

    val combinations = combine(msaList, aggregateReports) ++ combine(List(-1), nationalAggregateReports)

    val simpleReportFlow: Flow[(Int, AggregateReport), AggregateReportPayload, NotUsed] =
      Flow[(Int, AggregateReport)].mapAsyncUnordered(1) {
        case (msa, report) => report.generate(larSource, msa)
      }

    val s3Flow: Flow[AggregateReportPayload, CompletionStage[MultipartUploadResult], NotUsed] =
      Flow[AggregateReportPayload]
        .map(payload => {
          val filePath = s"$environment/reports/aggregate/2017/${payload.msa}/${payload.reportID}.txt"
          log.info(s"Publishing Aggregate report. MSA: ${payload.msa}, Report #: ${payload.reportID}")

          Source.single(ByteString(payload.report))
            .runWith(s3Client.multipartUpload(bucket, filePath))
        })

    Source(combinations).via(simpleReportFlow).via(s3Flow).runWith(Sink.ignore)
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

