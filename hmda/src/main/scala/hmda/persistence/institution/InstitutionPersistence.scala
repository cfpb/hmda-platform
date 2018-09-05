package hmda.persistence.institution

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import com.typesafe.config.ConfigFactory
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.model.institution.Institution

object InstitutionPersistence {

  final val name = "Institution"

  val config = ConfigFactory.load()

  val ShardingTypeName = EntityTypeKey[InstitutionCommand](name)
  val shardNumber = config.getInt("hmda.institutions.shardNumber")

  case class InstitutionState(institution: Option[Institution]) {
    def isEmpty: Boolean = institution.isEmpty
  }

  def behavior(entityId: String): Behavior[InstitutionCommand] =
    PersistentBehaviors
      .receive[InstitutionCommand, InstitutionEvent, InstitutionState](
        persistenceId = entityId,
        emptyState = InstitutionState(None),
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )
      .snapshotEvery(1000)
      .withTagger(_ => Set(name.toLowerCase()))

  val commandHandler
    : CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    (ctx, state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          if (!state.institution.contains(i)) {
            Effect.persist(InstitutionCreated(i)).andThen {
              ctx.log.debug(s"Institution Created: ${i.toString}")
              replyTo ! InstitutionCreated(i)
            }
          } else {
            Effect.none.andThen {
              ctx.log.debug(s"Institution already exists: ${i.toString}")
              replyTo ! InstitutionCreated(i)
            }
          }
        case ModifyInstitution(i, replyTo) =>
          if (state.institution.map(i => i.LEI).contains(i.LEI)) {
            Effect.persist(InstitutionModified(i)).andThen {
              ctx.log.debug(s"Institution Modified: ${i.toString}")
              replyTo ! InstitutionModified(i)
            }
          } else {
            Effect.none.andThen {
              ctx.log.warning(s"Institution with LEI: ${i.LEI} does not exist")
              replyTo ! InstitutionNotExists(i.LEI)
            }
          }
        case DeleteInstitution(lei, replyTo) =>
          if (state.institution.map(i => i.LEI).contains(lei)) {
            Effect.persist(InstitutionDeleted(lei)).andThen {
              ctx.log.debug(s"Institution Deleted: $lei")
              replyTo ! InstitutionDeleted(lei)
            }
          } else {
            Effect.none.andThen {
              ctx.log.warning(s"Institution with LEI: $lei does not exist")
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
