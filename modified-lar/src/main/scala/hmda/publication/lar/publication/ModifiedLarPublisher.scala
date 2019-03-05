package hmda.publication.lar.publication

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream._
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.config.ConfigFactory
import hmda.model.census.Census
import hmda.model.filing.submission.SubmissionId
import hmda.publication.lar.model.{
  EnrichedModifiedLoanApplicationRegister,
  ModifiedLoanApplicationRegister
}
import hmda.publication.lar.parser.ModifiedLarCsvParser
import hmda.publication.lar.repositories.ModifiedLarRepository
import hmda.query.HmdaQuery._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

sealed trait ModifiedLarCommand
case class PersistToS3AndPostgres(submissionId: SubmissionId,
                                  respondTo: ActorRef[PersistModifiedLarResult])
  extends ModifiedLarCommand
sealed trait UploadStatus
case object UploadSucceeded extends UploadStatus
case class UploadFailed(exception: Throwable) extends UploadStatus
case class PersistModifiedLarResult(submissionId: SubmissionId,
                                    status: UploadStatus)

object ModifiedLarPublisher {

  final val name: String = "ModifiedLarPublisher"

  val config = ConfigFactory.load()

  val filingYear = config.getInt("hmda.filing.year") // resides in common
  val accessKeyId = config.getString("aws.access-key-id")
  val secretAccess = config.getString("aws.secret-access-key ")
  val region = config.getString("aws.region")
  val bucket = config.getString("aws.public-bucket")
  val environment = config.getString("aws.environment")
  val year = config.getInt("hmda.lar.modified.year")
  val bankFilter = ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")
  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  def behavior(
                indexTractMap: Map[String, Census],
                modifiedLarRepo: ModifiedLarRepository): Behavior[ModifiedLarCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      val decider: Supervision.Decider = { e: Throwable =>
        log.error(e.getLocalizedMessage)
        Supervision.Resume
      }
      implicit val system: ActorSystem = ctx.system.toUntyped
      implicit val materializer: ActorMaterializer = ActorMaterializer(
        ActorMaterializerSettings(system).withSupervisionStrategy(decider))
      implicit val ec: ExecutionContext = ctx.system.toUntyped.dispatcher

      log.info(s"Started $name")

      val s3Settings = S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        pathStyleAccess = true,
        None,
        ListBucketVersion2
      )

      val s3Client: S3Client = new S3Client(s3Settings)

      Behaviors.receiveMessage {

        case PersistToS3AndPostgres(submissionId, respondTo) =>
          log.info(s"Publishing Modified LAR for $submissionId")

          val fileName = s"${submissionId.lei}.txt"

          val s3Sink = s3Client.multipartUpload(
            bucket,
            s"$environment/modified-lar/$year/$fileName")

          def removeLei: Future[Int] =
            modifiedLarRepo.deleteByLei(submissionId.lei, filingYear)

          val mlarSource: Source[ModifiedLoanApplicationRegister, NotUsed] =
            readRawData(submissionId)
              .map(l => l.data)
              .drop(1)
              .map(s => ModifiedLarCsvParser(s))

          val s3Out: Sink[ModifiedLoanApplicationRegister,
            Future[MultipartUploadResult]] =
            Flow[ModifiedLoanApplicationRegister]
              .map(mlar => mlar.toCSV + "\n")
              .map(ByteString(_))
              .toMat(s3Sink)(Keep.right)

          def postgresOut(parallelism: Int)
          : Sink[ModifiedLoanApplicationRegister, Future[Done]] =
            Flow[ModifiedLoanApplicationRegister]
              .map(
                mlar =>
                  EnrichedModifiedLoanApplicationRegister(
                    mlar,
                    indexTractMap.getOrElse(mlar.tract, Census())
                  )
              )
              .mapAsync(parallelism)(enriched =>
                modifiedLarRepo
                  .insert(enriched, submissionId.toString, filingYear))
              .toMat(Sink.ignore)(Keep.right)

          val graph = mlarSource
            .alsoToMat(postgresOut(2))(Keep.right) // TODO: provide a way to make this configurable
            .toMat(s3Out)(Keep.both)
            .mapMaterializedValue {
              // We listen on the completion of both materialized values but we only keep the S3 as the result
              // since that is a meaningful value
              case (futPostgresRes, futS3Res) =>
                for {
                  _ <- futPostgresRes
                  s3Res <- futS3Res
                } yield s3Res
            }

          val finalResult: Future[Unit] = for {
            _ <- removeLei
            _ <- graph.run()
          } yield ()

          finalResult.onComplete {
            case Success(_) =>
              log.info("Successfully completed persisting for {}", submissionId)
              respondTo ! PersistModifiedLarResult(submissionId,
                UploadSucceeded)

            case Failure(exception) =>
              log.error(
                s"Failed to delete and persist records for $submissionId {}",
                exception)
              respondTo ! PersistModifiedLarResult(submissionId,
                UploadFailed(exception))
              // bubble this up to the supervisor
              throw exception
          }

          Behaviors.same

        case _ =>
          Behaviors.ignore
      }
    }
}