package hmda.persistence.institution

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{
  ClusterSharding,
  EntityTypeKey,
  ShardedEntity
}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import com.typesafe.config.ConfigFactory
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.model.institution.Institution
import scala.concurrent.duration._

object InstitutionPersistence {

  final val name = "Institution"

  val config = ConfigFactory.load()

  val typeKey = EntityTypeKey[InstitutionCommand](name)
  val shardNumber = config.getInt("hmda.institutions.shardNumber")

  case class InstitutionState(institution: Option[Institution]) {
    def isEmpty: Boolean = institution.isEmpty
  }

  def behavior(entityId: String): Behavior[InstitutionCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[InstitutionCommand, InstitutionEvent, InstitutionState](
          persistenceId = entityId,
          emptyState = InstitutionState(None),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
        .withTagger(_ => Set(name.toLowerCase()))
    }

  def commandHandler(ctx: ActorContext[InstitutionCommand])
    : CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    val log = ctx.asScala.log
    (state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          if (!state.institution.contains(i)) {
            Effect.persist(InstitutionCreated(i)).thenRun { _ =>
              log.debug(s"Institution Created: ${i.toString}")
              replyTo ! InstitutionCreated(i)
            }
          } else {
            Effect.none.thenRun { _ =>
              log
                .debug(s"Institution already exists: ${i.toString}")
              replyTo ! InstitutionCreated(i)
            }
          }
        case ModifyInstitution(i, replyTo) =>
          if (state.institution.map(i => i.LEI).contains(i.LEI)) {
            Effect.persist(InstitutionModified(i)).thenRun { _ =>
              log.debug(s"Institution Modified: ${i.toString}")
              replyTo ! InstitutionModified(i)
            }
          } else {
            Effect.none.thenRun { _ =>
              log
                .warning(s"Institution with LEI: ${i.LEI} does not exist")
              replyTo ! InstitutionNotExists(i.LEI)
            }
          }
        case DeleteInstitution(lei, replyTo) =>
          if (state.institution.map(i => i.LEI).contains(lei)) {
            Effect.persist(InstitutionDeleted(lei)).thenRun { _ =>
              log.debug(s"Institution Deleted: $lei")
              replyTo ! InstitutionDeleted(lei)
            }
          } else {
            Effect.none.thenRun { _ =>
              log
                .warning(s"Institution with LEI: $lei does not exist")
              replyTo ! InstitutionNotExists(lei)
            }
          }
        case GetInstitution(replyTo) =>
          replyTo ! state.institution
          Effect.none
        case InstitutionStop =>
          Effect.stop
      }
  }

  val eventHandler: (InstitutionState, InstitutionEvent) => InstitutionState = {
    case (state, InstitutionCreated(i))   => state.copy(Some(i))
    case (state, InstitutionModified(i))  => modifyInstitution(i, state)
    case (state, InstitutionDeleted(_))   => state.copy(None)
    case (state, InstitutionNotExists(_)) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[InstitutionCommand]] = {
    sharding.start(
      ShardedEntity(
        create = entityId => supervisedBehavior(entityId),
        typeKey = typeKey,
        stopMessage = InstitutionStop
      ))
  }

  private def supervisedBehavior(
      entityId: String): Behavior[InstitutionCommand] = {
    val minBacOff = config.getInt("hmda.supervisor.minBackOff")
    val maxBacOff = config.getInt("hmda.supervisor.maxBackOff")
    val rFactor = config.getInt("hmda.supervisor.randomFactor")
    val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
      minBackoff = minBacOff.seconds,
      maxBackoff = maxBacOff.seconds,
      randomFactor = rFactor
    )

    Behaviors
      .supervise(InstitutionPersistence.behavior(entityId))
      .onFailure(supervisorStrategy)
  }

  private def modifyInstitution(institution: Institution,
                                state: InstitutionState): InstitutionState = {
    if (state.isEmpty) {
      state
    } else {
      if (institution.LEI == state.institution.get.LEI) {
        state.copy(Some(institution))
      } else {
        state
      }
    }
  }

}
