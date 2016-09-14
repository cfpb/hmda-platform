package hmda.persistence

import java.util.concurrent.TimeUnit

import akka.actor.ReceiveTimeout
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.CommonMessages.{ Event, Shutdown }

import scala.concurrent.duration.Duration

abstract class HmdaPersistentActor extends PersistentActor with HmdaActor {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def updateState(event: Event): Unit

  override def preStart(): Unit = {
    super.preStart()
    context.setReceiveTimeout(Duration.create(1, TimeUnit.MINUTES))
  }

  override def receiveCommand: Receive = {
    case ReceiveTimeout =>
      self ! Shutdown

    case Shutdown =>
      context stop self
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}
