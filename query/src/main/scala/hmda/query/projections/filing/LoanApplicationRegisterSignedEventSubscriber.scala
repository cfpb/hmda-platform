package hmda.query.projections.filing

import akka.actor.{ ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.stream.Supervision.Decider
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.processing.HmdaQuery._
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

object LoanApplicationRegisterSignedEventSubscriber {
  val name = "LoanApplicationRegisterSignedEventSubscriber"
  def props(): Props = Props(new LoanApplicationRegisterSignedEventSubscriber())
}

class LoanApplicationRegisterSignedEventSubscriber() extends HmdaActor with LoanApplicationRegisterCassandraRepository {

  val decider: Decider = { e =>
    repositoryLog.error("Unhandled error in stream", e)
    Supervision.Resume
  }

  override implicit def system: ActorSystem = context.system
  val materializerSettings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  override implicit def materializer: ActorMaterializer = ActorMaterializer(materializerSettings)(system)

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)

  override def receive: Receive = {

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"${self.path} subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      log.info(s"${self.path} received submission signed event with submission id: ${submissionId.toString}")
      val persistenceId = s"HmdaFileValidator-$submissionId"
      val larSource = events(persistenceId).map {
        case LarValidated(lar, _) => lar
        case _ => LoanApplicationRegister()
      }

      larSource
        .filter(lar => !lar.isEmpty)
        .map { lar => log.debug(s"Inserted: ${lar.toString}"); lar }
        .runWith(sink)

    case _ => //do nothing

  }

}
