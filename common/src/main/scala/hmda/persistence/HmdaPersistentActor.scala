package hmda.persistence

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.reflect.ClassTag

abstract class HmdaPersistentActor[Command, Event, State] {

  val name: String

  def typeKey(implicit tag: ClassTag[Command]): EntityTypeKey[Command] =
    EntityTypeKey[Command](name)

  def behavior(entityId: String): Behavior[Command]

  val eventHandler: (State, Event) => State

  protected def supervisedBehavior(entityId: String): Behavior[Command] = {
    val config = ConfigFactory.load()
    val minBackOff = config.getInt("hmda.supervisor.minBackOff")
    val maxBackOff = config.getInt("hmda.supervisor.maxBackOff")
    val rFactor = config.getDouble("hmda.supervisor.randomFactor")

    val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
      minBackoff = minBackOff.seconds,
      maxBackoff = maxBackOff.seconds,
      randomFactor = rFactor
    )

    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)

  }

}
