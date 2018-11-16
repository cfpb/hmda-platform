package hmda.persistence.submission

import akka.{Done, actor}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaTypedActor
import hmda.messages.submission.SubmissionCommands.ModifySubmission
import hmda.messages.submission.SubmissionEvents.{
  SubmissionEvent,
  SubmissionModified
}
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.messages.submission.SubmissionProcessingCommands.{
  StartParsing,
  StartQuality,
  StartSyntacticalValidity
}
import hmda.model.filing.submission._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import hmda.messages.pubsub.KafkaTopics._
import akka.actor.typed.scaladsl.adapter._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object SubmissionManager extends HmdaTypedActor[SubmissionManagerCommand] {

  override val name: String = "SubmissionManager"

  override def behavior(entityId: String): Behavior[SubmissionManagerCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      log.info(s"Started $entityId")

      val sharding = ClusterSharding(ctx.system)

      val submissionId = entityId.replaceAll(s"$name-", "")

      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey,
                              s"${SubmissionPersistence.name}-$submissionId")

      val hmdaParserError =
        sharding.entityRefFor(HmdaParserError.typeKey,
                              s"${HmdaParserError.name}-$submissionId")

      val hmdaValidationError =
        sharding.entityRefFor(HmdaValidationError.typeKey,
                              s"${HmdaValidationError.name}-$submissionId")

      val submissionEventResponseAdapter: ActorRef[SubmissionEvent] =
        ctx.messageAdapter(response => WrappedSubmissionEventResponse(response))

      Behaviors.receiveMessage {
        case UpdateSubmissionStatus(modified) =>
          submissionPersistence ! ModifySubmission(
            modified,
            submissionEventResponseAdapter)
          Behaviors.same

        case WrappedSubmissionEventResponse(submissionEvent) =>
          submissionEvent match {
            case SubmissionModified(submission) =>
              implicit val system: ActorSystem[_] = ctx.system
              submission.status match {
                case Uploaded =>
                  publishSubmissionModified(submissionTopic, submission)
                  hmdaParserError ! StartParsing(submission.id)
                case Parsed =>
                  publishSubmissionModified(submissionTopic, submission)
                  hmdaValidationError ! StartSyntacticalValidity(submission.id)
                case SyntacticalOrValidity =>
                  publishSubmissionModified(submissionTopic, submission)
                  hmdaValidationError ! StartQuality(submission.id)
                case Quality | QualityErrors =>
                  publishSubmissionModified(submissionTopic, submission)
                //TODO: Start macro edits

                case _ =>
              }
              Behaviors.same
            case _ =>
              log.info(s"$submissionEvent")
              Behaviors.same
          }
        case SubmissionManagerStop =>
          Behaviors.stopped
      }

    }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionManagerCommand]] = {
    super.startShardRegion(sharding)
  }

  private def publishSubmissionModified(topic: String, submission: Submission)(
      implicit system: ActorSystem[_]): Unit = {
    implicit val unTypedSystem: actor.ActorSystem = system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.toUntyped.dispatcher
    val config = ConfigFactory.load()
    val kafkaHosts = config.getString("kafka.hosts")
    val kafkaConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings =
      ProducerSettings(kafkaConfig, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaHosts)

    val kafkaProducer = producerSettings.createKafkaProducer()

    Source
      .single(submission.status)
      .map(_.asJson.noSpaces)
      .map(
        value =>
          new ProducerRecord[String, String](topic,
                                             submission.id.toString,
                                             value))
      .runWith(Producer.plainSink(producerSettings, kafkaProducer))
      .onComplete {
        case Success(_) =>
          kafkaProducer.close()
        case Failure(_) =>
          kafkaProducer.close()
      }
  }
}
