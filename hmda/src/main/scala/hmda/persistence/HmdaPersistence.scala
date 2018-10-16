package hmda.persistence

import akka.actor.typed.{Behavior, PostStop, PreRestart}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import com.typesafe.config.ConfigFactory
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.SubmissionPersistence

object HmdaPersistence {

  final val name = "HmdaPersistence"
  sealed trait HmdaPersistenceCommand
  case object StopHmdaPersistence extends HmdaPersistenceCommand

  val config = ConfigFactory.load()

  val behavior: Behavior[HmdaPersistenceCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")

      InstitutionPersistence.startShardRegion(ClusterSharding(ctx.system))
      FilingPersistence.startShardRegion(ClusterSharding(ctx.system))
      SubmissionPersistence.startShardRegion(ClusterSharding(ctx.system))

      Behaviors
        .receive[HmdaPersistenceCommand] {
          case (_, msg) =>
            msg match {
              case StopHmdaPersistence =>
                Behaviors.stopped
            }
        }
        .receiveSignal {
          case (c, PreRestart) =>
            c.log.info(s"Actor restarted at ${c.self.path}")
            Behaviors.same
          case (c, PostStop) =>
            c.log.info(s"Actor stopped at ${c.self.path}")
            Behaviors.same
        }
    }

}
