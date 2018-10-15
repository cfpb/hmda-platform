package hmda.persistence

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{
  ClusterSharding,
  EntityTypeKey,
  ShardedEntity
}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.reflect.ClassTag

abstract class HmdaPersistentActor[C, E, S] {

  val name: String

  def typeKey(implicit tag: ClassTag[C]): EntityTypeKey[C] =
    EntityTypeKey[C](name)

  def behavior(entityId: String): Behavior[C]

  val eventHandler: (S, E) => S

  protected def supervisedBehavior(entityId: String): Behavior[C] = {
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

  def startShardRegion(sharding: ClusterSharding, stopMessage: C)(
      implicit tag: ClassTag[C]): ActorRef[ShardingEnvelope[C]] = {
    sharding.start(
      ShardedEntity(
        create = entityId => supervisedBehavior(entityId),
        typeKey = typeKey,
        stopMessage = stopMessage
      )
    )
  }
}
