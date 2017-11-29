package hmda.query.projections.filing

import akka.actor.{ ActorSystem, Props }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics
import hmda.persistence.processing.HmdaQuery._
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import hmda.query.repository.filing.FilingCassandraRepository

object SubmissionSignedEventQuerySubscriber {
  val name = "SubmissionSignedEventQuerySubscriber"
  def props(): Props = Props(new SubmissionSignedEventQuerySubscriber())
}

class SubmissionSignedEventQuerySubscriber() extends HmdaActor with FilingCassandraRepository {

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)

  def receive: Receive = {

    case s: String =>
      repositoryLog.info("Got {}", s)

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      repositoryLog.info(s"Subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      repositoryLog.info(s"${self.path} received submission signed event with submission id: ${submissionId.toString}")
      val persistenceId = s"HmdaFileValidator-$submissionId"
      val larSource = events(persistenceId).map {
        case LarValidated(lar, _) => lar
        case _ => LoanApplicationRegister()
      }

      larSource
        .filter(lar => !lar.isEmpty)
        .map { lar => repositoryLog.debug(s"Inserted: ${lar.toString}"); lar }
        .runWith(sink)

    case _ => //do nothing
  }

  override implicit def system: ActorSystem = context.system

  override implicit def materializer: ActorMaterializer = ActorMaterializer()
}
