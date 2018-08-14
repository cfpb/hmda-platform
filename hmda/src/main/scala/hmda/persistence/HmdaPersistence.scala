package hmda.persistence

import akka.actor.typed.{ActorContext, Behavior, PostStop, PreRestart, Props}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import com.typesafe.config.ConfigFactory
import hmda.messages.institution.InstitutionCommands.{
  InstitutionCommand,
  InstitutionStop
}
import hmda.persistence.institution.InstitutionPersistence

object HmdaPersistence {

  final val name = "HmdaPersistence"
  sealed trait HmdaPersistenceCommand
  case object StopHmdaPersistence extends HmdaPersistenceCommand

  val behavior: Behavior[HmdaPersistenceCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")

      startInstitutionsSharding(ctx)

      Behaviors
        .receive[HmdaPersistenceCommand] {
          case (_, msg) =>
            msg match {
              case StopHmdaPersistence =>
                Behaviors.stopped
            }
        }
        .receiveSignal {
          case (ctx, PreRestart) =>
            ctx.log.info(s"Actor restarted at ${ctx.self.path}")
            Behaviors.same
          case (ctx, PostStop) =>
            ctx.log.info(s"Actor stopped at ${ctx.self.path}")
            Behaviors.same
        }
    }

  private def startInstitutionsSharding(ctx: ActorContext[_]): Unit = {
    val typeKey = EntityTypeKey[InstitutionCommand](InstitutionPersistence.name)
    val config = ConfigFactory.load()
    val shardNumber = config.getInt("hmda.institutions.shardNumber")
    val system = ctx.asScala.system
    val sharding = ClusterSharding(system)
    sharding.spawn(
      behavior = entityId => InstitutionPersistence.behavior(entityId),
      Props.empty,
      typeKey,
      ClusterShardingSettings(system),
      maxNumberOfShards = shardNumber,
      handOffStopMessage = InstitutionStop
    )
  }

}
