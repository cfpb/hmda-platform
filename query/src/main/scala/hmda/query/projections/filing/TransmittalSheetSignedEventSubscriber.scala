package hmda.query.projections.filing

import java.time.LocalDateTime

import akka.actor.{ ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.stream.Supervision.Decider
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import hmda.model.fi.ts.TransmittalSheet
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.TsValidated
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.query.repository.filing.TransmittalSheetCassandraRepository
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.processing.PubSubTopics
import hmda.query.model.filing.TransmittalSheetWithTimestamp

object TransmittalSheetSignedEventSubscriber {
  val name = "TransmittalSheetSignedEventSubscriber"
  def props(): Props = Props(new TransmittalSheetSignedEventSubscriber)
}

class TransmittalSheetSignedEventSubscriber extends HmdaActor with TransmittalSheetCassandraRepository {

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  val sink = CassandraSink[TransmittalSheetWithTimestamp](parallelism = 2, preparedStatement, statementBinder)

  override def receive: Receive = {

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"${self.path} subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      log.info(s"${self.path} received submission signed event with submission id: ${submissionId.toString}")
      val perssitenceId = s"HmdaFileValidator-$submissionId"
      val tsSource = events(perssitenceId).map {
        case TsValidated(ts) => ts
        case _ => TransmittalSheet()
      }

      tsSource
        .filter(ts => !ts.isEmpty)
        .map { ts => log.info(s"Inserted: ${ts.toString}"); ts }
        .map(ts => TransmittalSheetWithTimestamp(ts, LocalDateTime.now().toString))
        .runWith(sink)

    case _ => //do nothing

  }

}
