package hmda.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait HmdaTypedActor[A] {

  val name: String

  def typeKey(implicit tag: ClassTag[A]): EntityTypeKey[A] =
    EntityTypeKey[A](name)

  def behavior(entityId: String): Behavior[A]

  protected def supervisedBehavior(entityId: String): Behavior[A] = {
    val config     = ConfigFactory.load()
    val minBackOff = config.getInt("hmda.supervisor.minBackOff")
    val maxBackOff = config.getInt("hmda.supervisor.maxBackOff")
    val rFactor    = config.getDouble("hmda.supervisor.randomFactor")

    val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
      minBackoff = minBackOff.seconds,
      maxBackoff = maxBackOff.seconds,
      randomFactor = rFactor
    )

    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)
  }

  def startShardRegion(sharding: ClusterSharding, stopMessage: A)(implicit tag: ClassTag[A]): ActorRef[ShardingEnvelope[A]] =
    sharding.init(
      Entity(typeKey = typeKey)(createBehavior = ctx => supervisedBehavior(ctx.entityId))
        .withStopMessage(stopMessage)
    )
}