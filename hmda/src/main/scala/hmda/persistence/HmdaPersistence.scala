package hmda.persistence

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ Behavior, PostStop, PreRestart }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import com.typesafe.config.ConfigFactory
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission._

// This is just a guardian
// $COVERAGE-OFF$
object HmdaPersistence {

  final val name = "HmdaPersistence"
  sealed trait HmdaPersistenceCommand
  case object StopHmdaPersistence extends HmdaPersistenceCommand

  val config = ConfigFactory.load()

  def apply(): Behavior[HmdaPersistenceCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      val sharding = ClusterSharding(ctx.system)

      InstitutionPersistence.startShardRegion(sharding)
      FilingPersistence.startShardRegion(sharding)
      SubmissionPersistence.startShardRegion(sharding)
      HmdaRawData.startShardRegion(sharding)
      SubmissionManager.startShardRegion(sharding)
      HmdaParserError.startShardRegion(sharding)
      HmdaValidationError.startShardRegion(sharding)
      EditDetailsPersistence.startShardRegion(sharding)

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
// $COVERAGE-ON$