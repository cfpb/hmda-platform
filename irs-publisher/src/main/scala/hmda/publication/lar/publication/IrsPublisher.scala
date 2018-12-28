package hmda.publication.lar.publication

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.parser.filing.lar.LarCsvParser
import hmda.publication.lar.model.{MsaMap, MsaSummary}
import hmda.query.HmdaQuery._

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

      Behaviors.receiveMessage {

        case PublishIrs(submissionId) =>
          log.info(s"Publishing IRS for $submissionId")

          val s3Sink = s3Client.multipartUpload(
            bucket,
            s"$environment/reports/disclosure/$year/${submissionId.lei}/nationwide/IRS.txt")

          val msaMapF = readRawData(submissionId)
            .map(l => l.data)
            .drop(1)
            .map(s => LarCsvParser(s).getOrElse(LoanApplicationRegister()))
            .fold(MsaMap())((map, lar) => map + lar)
            .runWith(Sink.last)

          msaMapF.onComplete(_ => {
            case Success(msaMap: MsaMap) =>
              log.info(s"Uploading IRS to S3 for $submissionId")
              val msaSeq = msaMap.msas.values.toSeq
              val msaSummary = MsaSummary.fromMsaCollection(msaSeq)
              //TODO: Maybe add a header row here?
              val bytes = msaSeq.map(msa => ByteString(msa.toCsv + "\n")) :+ ByteString(msaSummary.toCsv)
              Source(bytes.toList).runWith(s3Sink)
            case Failure(e) => log.error(s"Reading Cassandra journal failed: $e")
          })

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}
