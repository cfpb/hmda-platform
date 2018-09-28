package hmda.persistence.institution

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.model.institution.Institution
import hmda.persistence.HmdaPersistentActor

object InstitutionPersistence
    extends HmdaPersistentActor[InstitutionCommand,
                                InstitutionEvent,
                                InstitutionState] {

  final val name = "Institution"

  override def behavior(entityId: String): Behavior[InstitutionCommand] = {
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started Institution: $entityId")
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

  override val eventHandler
    : (InstitutionState, InstitutionEvent) => InstitutionState = {
    case (state, InstitutionCreated(i))   => state.copy(Some(i))
    case (state, InstitutionModified(i))  => modifyInstitution(i, state)
    case (state, InstitutionDeleted(_))   => state.copy(None)
    case (state, InstitutionNotExists(_)) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[InstitutionCommand]] = {
    super.startShardRegion(sharding, InstitutionStop)
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
