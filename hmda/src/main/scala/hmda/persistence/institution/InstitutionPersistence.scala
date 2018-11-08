package hmda.persistence.institution

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.model.institution.{Institution, InstitutionDetail}
import hmda.persistence.HmdaTypedPersistentActor

object InstitutionPersistence
    extends HmdaTypedPersistentActor[InstitutionCommand,
                                     InstitutionEvent,
                                     InstitutionState] {

  override final val name = "Institution"

  override def behavior(entityId: String): Behavior[InstitutionCommand] = {
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started Institution: $entityId")
      PersistentBehavior[InstitutionCommand,
                         InstitutionEvent,
                         InstitutionState](
        persistenceId = PersistenceId(s"$entityId"),
        emptyState = InstitutionState(None),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).snapshotEvery(1000)
        .withTagger(_ => Set(name.toLowerCase()))
    }
  }

  override def commandHandler(ctx: ActorContext[InstitutionCommand])
    : CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    val log = ctx.asScala.log
    (state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          if (state.institution.isEmpty) {
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
        case AddFiling(filing, replyTo) =>
          Effect.persist(FilingAdded(filing)).thenRun { _ =>
            log.debug(s"Added Filing: ${filing.toString}")
            replyTo match {
              case Some(ref) => ref ! filing
              case None      => Effect.none
            }
          }

        case GetInstitutionDetails(replyTo) =>
          if (state.institution.isEmpty) {
            replyTo ! None
          } else {
            replyTo ! Some(InstitutionDetail(state.institution, state.filings))
          }
          Effect.none

        case GetInstitution(replyTo) =>
          replyTo ! state.institution
          Effect.none
        case InstitutionStop =>
          Effect.stop()
      }
  }

  override val eventHandler
    : (InstitutionState, InstitutionEvent) => InstitutionState = {
    case (state, InstitutionCreated(i))   => state.copy(Some(i))
    case (state, InstitutionModified(i))  => modifyInstitution(i, state)
    case (state, InstitutionDeleted(_))   => state.copy(None)
    case (state, evt @ FilingAdded(_))    => state.update(evt)
    case (state, InstitutionNotExists(_)) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[InstitutionCommand]] = {
    super.startShardRegion(sharding)
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
