package hmda.persistence

import akka.actor.typed.{
  ActorContext,
  Behavior,
  PostStop,
  PreRestart
}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey

object HmdaPersistence {

  final val name = "HmdaPersistence"


  sealed trait HmdaPersistenceCommand
  case object StopHmdaPersistence extends HmdaPersistenceCommand

  val ShardingTypeName = EntityTypeKey[HmdaPersistenceCommand](name)
  val MaxNumberOfShards = 10

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

  def shardingBehavior: Behavior[HmdaPersistenceCommand] = ???

  def startInstitutionsSharding(ctx: ActorContext[_]): Unit = {

  }

}
