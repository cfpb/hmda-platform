package hmda.persistence.model

import java.util.concurrent.TimeUnit

import akka.actor.ReceiveTimeout
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.messages.CommonMessages.{ Event, Shutdown }
import hmda.persistence.PersistenceConfig._
import scala.concurrent.duration.Duration

abstract class HmdaPersistentActor extends PersistentActor with HmdaActor {

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def updateState(event: Event): Unit

  override def preStart(): Unit = {
    super.preStart()
    val timeout = configuration.getInt("hmda.persistent-actor-timeout")
    context.setReceiveTimeout(Duration.create(timeout, TimeUnit.SECONDS))
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
