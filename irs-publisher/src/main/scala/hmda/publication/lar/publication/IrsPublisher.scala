package hmda.publication.lar.publication

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.census.Census
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.lar.LarCsvParser
import hmda.publication.lar.model.{Msa, MsaSummary}
import hmda.query.HmdaQuery._
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

sealed trait IrsPublisherCommand
case class PublishIrs(submissionId: SubmissionId) extends IrsPublisherCommand

object IrsPublisher {

  final val name: String = "IrsPublisher"

  val config = ConfigFactory.load()

  val accessKeyId = config.getString("aws.access-key-id")
  val secretAccess = config.getString("aws.secret-access-key ")
  val region = config.getString("aws.region")
  val bucket = config.getString("aws.public-bucket")
  val environment = config.getString("aws.environment")
  val year = config.getInt("hmda.lar.irs.year")

  val censusHost = config.getString("hmda.census.http.host")
  val censusPort = config.getInt("hmda.census.http.port")

  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val behavior: Behavior[IrsPublisherCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      val decider: Supervision.Decider = {
        case e: Throwable =>
          log.error(e.getLocalizedMessage)
          Supervision.Resume
      }
      implicit val ec = ctx.system.executionContext
      implicit val system = ctx.system.toUntyped
      implicit val materializer = ActorMaterializer(
        ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      log.info(s"Started $name")

      val s3Settings = new S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )

      val s3Client = new S3Client(s3Settings, system, materializer)

      def getCensus(hmdaCensus: String): Future[Msa] = {
        val request = HttpRequest(
          uri = s"http://$censusHost:$censusPort/census/tract/$hmdaCensus")

        for {
          r <- Http().singleRequest(request)
          census <- Unmarshal(r.entity).to[Census]
        } yield {
          val censusID = if (census.msaMd == 0) "-----" else census.msaMd.toString
          val censusName =
            if (census.name.isEmpty) "MSA/MD NOT AVAILABLE" else census.name
          Msa(censusID, censusName)
        }
      }

      Behaviors.receiveMessage {

        case PublishIrs(submissionId) =>
          log.info(s"Publishing IRS for $submissionId")

          val s3Sink = s3Client.multipartUpload(
            bucket,
            s"$environment/reports/disclosure/$year/${submissionId.lei}/nationwide/IRS.csv")

          val futMsaList = readRawData(submissionId)
            .drop(1)
            .map(l => l.data)
            .map(s => LarCsvParser(s).getOrElse(LoanApplicationRegister()))
            .throttle(32, 1.second)
            .mapAsync(1)(lar => getCensus(lar.geography.tract))
            .runWith(Sink.collection)

          futMsaList.onComplete {
            case Success(list) =>
              log.info(s"Uploading IRS to S3 for $submissionId")
              val msaSummary = MsaSummary.fromMsaCollection(list)
              val header =
                "MSA/MD, MSA/MD Name, Total Lars, Total Amount ($000's), CONV, FHA, VA, FSA, Site Built, Manufactured, 1-4 units, 5+ units, Home Purchase, Home Improvement, Refinancing, Cash-out Refinancing, Other Purpose, Purpose N/A\n"
              val bytes = ByteString(header) +:
                list.map(msa => ByteString(msa.toCsv + "\n")) :+
                ByteString(msaSummary.toCsv)
              Source(bytes.toList).runWith(s3Sink)
            case Failure(e) =>
              log.error(s"Reading Cassandra journal failed: $e")
          }

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}
