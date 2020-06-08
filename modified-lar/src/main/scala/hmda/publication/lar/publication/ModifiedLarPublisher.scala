package hmda.publication.lar.publication

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, SupervisorStrategy }
import akka.stream._
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{ Done, NotUsed }
import com.typesafe.config.ConfigFactory
import hmda.messages.pubsub.HmdaTopics._
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.model.census.Census
import hmda.model.filing.submission.SubmissionId
import hmda.model.modifiedlar.{ EnrichedModifiedLoanApplicationRegister, ModifiedLoanApplicationRegister }
import hmda.publication.KafkaUtils
import hmda.publication.KafkaUtils._
import hmda.publication.lar.parser.ModifiedLarCsvParser
import hmda.query.HmdaQuery
import hmda.query.HmdaQuery._
import hmda.query.repository.ModifiedLarRepository
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

sealed trait ModifiedLarCommand
case class PersistToS3AndPostgres(submissionId: SubmissionId, respondTo: ActorRef[PersistModifiedLarResult]) extends ModifiedLarCommand
sealed trait UploadStatus
case object UploadSucceeded                   extends UploadStatus
case class UploadFailed(exception: Throwable) extends UploadStatus
case class PersistModifiedLarResult(submissionId: SubmissionId, status: UploadStatus)

object ModifiedLarPublisher {

  final val name: String = "ModifiedLarPublisher"

  val config                    = ConfigFactory.load()
  val accessKeyId               = config.getString("aws.access-key-id")
  val secretAccess              = config.getString("aws.secret-access-key ")
  val region                    = config.getString("aws.region")
  val bucket                    = config.getString("aws.public-bucket")
  val environment               = config.getString("aws.environment")
  val isGenerateS3File          = config.getBoolean("hmda.lar.modified.generateS3Files")
  val isCreateDispositionRecord = config.getBoolean("hmda.lar.modified.creteDispositionRecord")
  val isJustGenerateS3File = config.getBoolean("hmda.lar.modified.justGenerateS3File")
  val isJustGenerateS3FileHeader = config.getBoolean("hmda.lar.modified.justGenerateS3FileHeader")

  val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
  val awsRegionProvider: AwsRegionProvider = new AwsRegionProvider {
    override def getRegion: Region = Region.of(region)
  }

  def behavior(
                indexTractMap2018: Map[String, Census],
                indexTractMap2019: Map[String, Census],
                modifiedLarRepo: ModifiedLarRepository,
                readRawData: ActorSystem[_] => SubmissionId => Source[LineAdded, NotUsed] = as => id => HmdaQuery.readRawData(id)(as)
              ): Behavior[ModifiedLarCommand] =
    Behaviors.setup { ctx =>
      val log                                 = ctx.log
      implicit val system: ActorSystem[_]     = ctx.system
      implicit val materializer: Materializer = Materializer(ctx)
      implicit val ec: ExecutionContext       = ctx.executionContext

      log.info(s"Started $name")

      val s3Settings = S3Settings(ctx.system.toClassic)
        .withBufferType(MemoryBufferType)
        .withCredentialsProvider(awsCredentialsProvider)
        .withS3RegionProvider(awsRegionProvider)
        .withListBucketApiVersion(ListBucketVersion2)

      val kafkaProducer = KafkaUtils.getStringKafkaProducer(ctx.system)

      Behaviors
        .supervise[ModifiedLarCommand] {
          Behaviors.receiveMessage {

            case PersistToS3AndPostgres(submissionId, respondTo) =>
              log.info(
                s"Publishing Modified LAR for $submissionId with isGenerateS3File set to " + isGenerateS3File +
                  "and isCreateDispositionRecord set to " + isCreateDispositionRecord
              )

              val fileName       = s"${submissionId.lei.toUpperCase()}.txt"
              val fileNameHeader = s"${submissionId.lei.toUpperCase()}_header.txt"
              val filingPeriod   = s"${submissionId.period}"

              val metaHeaders: Map[String, String] =
                Map("Content-Disposition" -> "attachment", "filename" -> fileName)

              val s3Sink = S3
                .multipartUpload(bucket, s"$environment/modified-lar/$filingPeriod/$fileName", metaHeaders = MetaHeaders(metaHeaders))
                .withAttributes(S3Attributes.settings(s3Settings))

              val s3SinkWithHeader = S3
                .multipartUpload(
                  bucket,
                  s"$environment/modified-lar/$filingPeriod/header/$fileNameHeader",
                  metaHeaders = MetaHeaders(metaHeaders)
                )
                .withAttributes(S3Attributes.settings(s3Settings))

              def removeLei: Future[Int] =
                modifiedLarRepo.deleteByLei(submissionId)

              val mlarSource: Source[ModifiedLoanApplicationRegister, NotUsed] =
                readRawData(system)(submissionId)
                  .map(l => l.data)
                  .drop(1)
                  .map(s => ModifiedLarCsvParser(s, submissionId.period.year))

              val serializeMlar: Flow[ModifiedLoanApplicationRegister, ByteString, NotUsed] = {
                Flow[ModifiedLoanApplicationRegister]
                  .map(mlar => mlar.toCSV + "\n")
                  .map(ByteString(_))
              }

              def postgresOut(parallelism: Int): Sink[ModifiedLoanApplicationRegister, Future[Done]] =
                Flow[ModifiedLoanApplicationRegister].map { mlar =>
                  val indexTractMap = if (submissionId.period.year == 2018) indexTractMap2018 else indexTractMap2019
                  EnrichedModifiedLoanApplicationRegister(
                    mlar,
                    indexTractMap.getOrElse(mlar.tract, Census())
                  )
                }.mapAsync(parallelism)(enriched =>
                  modifiedLarRepo
                    .insert(enriched, submissionId)
                )
                  .toMat(Sink.ignore)(Keep.right)

              val mlarGraphS3: RunnableGraph[Future[Done]] =
                RunnableGraph.fromGraph(
                  GraphDSL.create(mlarSource, s3SinkWithHeader, s3Sink, postgresOut(2))((_, s3HeaderMat, s3NoHeaderMat, pgMat) =>
                    for {
                      _ <- s3HeaderMat
                      _ <- s3NoHeaderMat
                      _ <- pgMat
                    } yield akka.Done.done()
                  ) { implicit builder => (source, headerSink, noHeaderSink, pgSink) =>
                    import GraphDSL.Implicits._

                    val mlarHeader = Source.single(ByteString(ModifiedLoanApplicationRegister.header))
                    val broadcast  = builder.add(Broadcast[ModifiedLoanApplicationRegister](3))

                    source.out ~> broadcast.in
                    (broadcast.out(0) ~> serializeMlar).prepend(mlarHeader) ~> headerSink
                    broadcast.out(1) ~> serializeMlar ~> noHeaderSink
                    broadcast.out(2) ~> pgSink

                    ClosedShape
                  }
                )

              def mlarGraphWithoutS3: RunnableGraph[Future[Done]] =
                mlarSource.toMat(postgresOut(2))(Keep.right)

              // write to both Postgres and S3
              val graphWithS3 = mlarGraphS3

              //only write to PG - do not generate S3 files
              val graphWithoutS3 = mlarGraphWithoutS3

              val graphWithJustS3NoHeader = <how to get graph just for S3 file with no header>

              val graphWithJustS3WithHeader = <how to get graph with just S3 file with header>

              val finalResult: Future[Unit] = for {
                _ <- removeLei
                _ <- if (isGenerateS3File) graphWithS3.run()
                else graphWithoutS3.run()
                _ <- produceRecord(disclosureTopic, submissionId.lei, submissionId.toString, kafkaProducer)
              } yield ()

              finalResult.onComplete {
                case Success(_) =>
                  log.info("Successfully completed persisting for {}", submissionId)
                  respondTo ! PersistModifiedLarResult(submissionId, UploadSucceeded)

                case Failure(exception) =>
                  log.error(s"Failed to delete and persist records for $submissionId {}", exception)
                  respondTo ! PersistModifiedLarResult(submissionId, UploadFailed(exception))
                  // bubble this up to the supervisor
                  throw exception
              }

              Behaviors.same

            case _ =>
              Behaviors.ignore
          }
        }
        .onFailure(SupervisorStrategy.resume.withLoggingEnabled(true))
    }
}