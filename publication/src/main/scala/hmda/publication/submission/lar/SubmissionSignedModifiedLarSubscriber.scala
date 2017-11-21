package hmda.publication.submission.lar

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.util.{ ByteString, Timeout }
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics
import hmda.query.repository.filing.FilingCassandraRepository
import hmda.persistence.processing.HmdaQuery._
import hmda.query.repository.filing.LarConverter._

import scala.concurrent.duration._

object SubmissionSignedModifiedLarSubscriber {
  val name = "SubmissionSignedModifiedLarSubscriber"
  def props(supervisor: ActorRef): Props = Props(new SubmissionSignedModifiedLarSubscriber(supervisor))
}

class SubmissionSignedModifiedLarSubscriber(supervisor: ActorRef) extends HmdaActor with FilingCassandraRepository {

  override implicit def system: ActorSystem = context.system

  override implicit def materializer: ActorMaterializer = ActorMaterializer()

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  val fetchSize = config.getInt("hmda.publication.fetchsize")
  val actorTimeout = config.getInt("hmda.actor.timeout")

  implicit val timeout = Timeout(actorTimeout.seconds)

  val accessKeyId = config.getString("hmda.publication.aws.access-key-id")
  val secretAccess = config.getString("hmda.publication.aws.secret-access-key ")
  val region = config.getString("hmda.publication.aws.region")
  val bucket = config.getString("hmda.publication.aws.public-bucket")

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings, context.system, materializer)

  def receive: Receive = {
    case s: String =>
      log.info("Got {}", s)

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info("Subscribed to {}", PubSubTopics.submissionSigned)

    case SubmissionSignedPubSub(submissionId) =>
      val institutionId = submissionId.institutionId
      val fileName = s"$institutionId.csv"
      val s3Sink = s3Client.multipartUpload(bucket, fileName)
      log.info(s"${self.path} received submission signed event with submission id: ${submissionId.toString}")
      val persistenceId = s"HmdaFileValidator-$submissionId"
      val larSource = events(persistenceId).map {
        case LarValidated(lar, _) => lar
        case _ => LoanApplicationRegister()
      }

      larSource
        .filter(lar => !lar.isEmpty)
        .map(lar => toLoanApplicationRegisterQuery(lar))
        .map(mLar => mLar.toCSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

    case _ => //do nothing

  }

}
