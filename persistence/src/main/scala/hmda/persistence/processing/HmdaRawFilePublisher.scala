package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.persistence.PersistentActor

object HmdaRawFilePublisher {
  def props(id: String): Props = Props(new HmdaRawFilePublisher(id))

  def createHmdaRawFilePublisher(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaRawFilePublisher.props(submissionId))
  }
}

class HmdaRawFilePublisher(submissionId: String) extends PersistentActor {
  override def receiveRecover: Receive = ???

  override def receiveCommand: Receive = ???

  override def persistenceId: String = ???
}
