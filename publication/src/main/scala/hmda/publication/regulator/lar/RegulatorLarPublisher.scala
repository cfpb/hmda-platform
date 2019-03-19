package hmda.publication.regulator.lar

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.concurrent.duration._
import akka.NotUsed
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.{ ActorRef, Props }
import akka.http.scaladsl.model.{ ContentType, HttpCharsets, MediaTypes }
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{ S3Headers, ServerSideEncryption }
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl.Flow
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import hmda.persistence.HmdaSupervisor.{ FindHmdaFilerPersistence, FindSubmissions }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.institutions.{ HmdaFilerPersistence, SubmissionPersistence }
import hmda.persistence.institutions.SubmissionPersistence.GetLatestSubmission
import hmda.persistence.messages.CommonMessages.GetState
import hmda.model.fi.Submission
import akka.util.ByteString
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import hmda.census.model.TractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.Signed
import hmda.model.institution.HmdaFiler
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.model.HmdaActor
import hmda.publication.regulator.messages._
import hmda.query.repository.filing.LarConverter._
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

import scala.concurrent._
import scala.util.{ Try, Failure, Success }
import scala.concurrent.ExecutionContext

object RegulatorLarPublisher {
  def props(supervisor: ActorRef): Props = Props(new RegulatorLarPublisher(supervisor))
}

class RegulatorLarPublisher(supervisor: ActorRef) extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  QuartzSchedulerExtension(system).schedule("LARRegulator", self, PublishRegulatorData)
  QuartzSchedulerExtension(system).schedule("DynamicLARRegulator", self, PublishDynamicData)

  val decider: Decider = { e =>
    log.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)
  override implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)

  val fetchSize = config.getInt("hmda.query.fetch.size")

  val accessKeyId = config.getString("hmda.publication.aws.access-key-id")
  val secretAccess = config.getString("hmda.publication.aws.secret-access-key ")
  val region = config.getString("hmda.publication.aws.region")
  val bucket = config.getString("hmda.publication.aws.private-bucket")
  val publicBucket = config.getString("hmda.publication.aws.public-bucket")
  val environment = config.getString("hmda.publication.aws.environment")
  val filteredRespondentIds = config.getString("hmda.publication.filtered-respondent-ids").split(",")
  val dynamicFilteredRespondentIds = config.getString("hmda.publication.dynamic-filtered-respondent-ids").split(",")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  val tractMap = TractLookup.valuesExtended.map(t => (t.tractDec + t.county + t.state, t.toCSV)).toMap

  override def receive: Receive = {

    case PublishRegulatorData =>
      val now = LocalDateTime.now()
      val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}_lar.txt"
      log.info(s"Uploading $fileName to S3")

      val ffSubmissions = getFilers(supervisor)
        .map(filerList => {
          filerList.map(filer => latestSubmission(supervisor, filer.institutionId))
        })

      ffSubmissions onComplete {
        case Success(fSubmissions) => {
          val bagel = Future.sequence(fSubmissions.map(futureToFutureTry(_))).map(_.collect { case Success(x) => x })
          bagel onComplete {
            case Success(submissions) => {
              submissions
                .map(_.id)
                .map(submissionId => {
                  val persistenceId = s"HmdaFileValidator-$submissionId"
                  val larSource = events(persistenceId).map {
                    case LarValidated(lar, _) => lar
                    case _ => LoanApplicationRegister()
                  }
                  val source = larSource
                    .via(filterTestBanks)
                    .map(lar => lar.toCSV + "\n")
                    .map(s => ByteString(s))

                  val s3Sink = s3Client.multipartUpload(
                    bucket,
                    s"$environment/test/lar/$submissionId.id.institutionId",
                    ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
                    S3Headers(ServerSideEncryption.AES256)
                  )

                  source.runWith(s3Sink)
                })
            }
            case Failure(error) =>
              log.error("Stream Failed", error)
          }
        }
        case Failure(error) =>
          log.error("Stream Failed", error)
      }

    case PublishDynamicData =>
      val fileName = "2017_lar.txt"
      log.info(s"Uploading $fileName to $environment/dynamic-data/$fileName")
      val s3Sink = s3Client.multipartUpload(
        publicBucket,
        s"$environment/dynamic-data/$fileName",
        ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
        S3Headers(Map("Content-Disposition" -> s"attachment; filename=$fileName;", "x-amz-server-side-encryption" -> "AES256"))
      )

      val source = readData(fetchSize)
        .via(filterTestBanks)
        .via(filterDynamicTestBanks)
        .map(lar => addCensusDataFromMap(lar))
        .map(s => ByteString(s))

      source.runWith(s3Sink)

    case _ => //do nothing
  }

  def getFilers(supervisor: ActorRef)(implicit ec: ExecutionContext): Future[List[HmdaFiler]] = {
    val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
    for {
      a <- hmdaFilerPersistenceF
      filers <- (a ? GetState).mapTo[Set[HmdaFiler]]
    } yield filers.toList

  }

  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] =
    f.map(Success(_)).recover({ case e => Failure(e) })

  def latestSubmission(supervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext): Future[Submission] = {
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, "2017")).mapTo[ActorRef]

    val fSubmissions = for {
      s <- fSubmissionsActor
      xs <- (s ? GetLatestSubmission).mapTo[Submission]
    } yield xs

    fSubmissions.map(submission =>
      if (submission.status != Signed) {
        log.error("No submission")
        submission
      } else {
        submission
      })
  }

  def addCensusDataFromMap(lar: LoanApplicationRegister): String = {
    val baseString = toModifiedLar(lar).toCSV
    val key = lar.geography.tract + lar.geography.county + lar.geography.state
    val tract = tractMap.getOrElse(key, "|||||")
    baseString + "|" + tract + "\n"
  }

  def filterTestBanks: Flow[LoanApplicationRegister, LoanApplicationRegister, NotUsed] = {
    Flow[LoanApplicationRegister]
      .filterNot(lar => filteredRespondentIds.contains(lar.respondentId) ||
        (lar.respondentId == "954623407" && lar.agencyCode == 9) ||
        (lar.respondentId == "1467" && lar.agencyCode == 1))
  }

  def filterDynamicTestBanks: Flow[LoanApplicationRegister, LoanApplicationRegister, NotUsed] = {
    Flow[LoanApplicationRegister]
      .filterNot(lar => dynamicFilteredRespondentIds.contains(lar.respondentId))
  }
}
