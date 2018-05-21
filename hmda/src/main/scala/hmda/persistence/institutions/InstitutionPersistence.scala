package hmda.persistence.institutions

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.model.institution.Institution

object InstitutionPersistence {

  final val name = "institution"

  sealed trait InstitutionCommand
  sealed trait InstitutionEvent
  case class CreateInstitution(i: Institution,
                               replyTo: ActorRef[InstitutionCreated])
      extends InstitutionCommand
  case class InstitutionCreated(i: Institution) extends InstitutionEvent
  case class ModifyInstitution(i: Institution,
                               replyTo: ActorRef[InstitutionModified])
      extends InstitutionCommand
  case class InstitutionModified(i: Institution) extends InstitutionEvent
  case class DeleteInstitution(LEI: String,
                               replyTo: ActorRef[InstitutionDeleted])
      extends InstitutionCommand
  case class InstitutionDeleted(LEI: String) extends InstitutionEvent
  case class Get(replyTo: ActorRef[Option[Institution]])
      extends InstitutionCommand

  case class InstitutionState(institution: Option[Institution]) {
    def isEmpty: Boolean = institution.isEmpty
  }

  def behavior(entityId: String): Behavior[InstitutionCommand] =
    PersistentBehaviors
      .receive[InstitutionCommand, InstitutionEvent, InstitutionState](
        persistenceId = s"$name-$entityId",
        initialState = InstitutionState(None),
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )
      .snapshotEvery(1000)
      .withTagger(_ => Set("institutions"))

  val commandHandler
    : CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    (ctx, state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          Effect.persist(InstitutionCreated(i)).andThen {
            ctx.log.debug(s"Institution Created: ${i.toString}")
            replyTo ! InstitutionCreated(i)
          }
        case ModifyInstitution(i, replyTo) =>
          Effect.persist(InstitutionModified(i)).andThen {
            ctx.log.debug(s"Institution Modified: ${i.toString}")
            replyTo ! InstitutionModified(i)
          }
        case DeleteInstitution(lei, replyTo) =>
          Effect.persist(InstitutionDeleted(lei)).andThen {
            ctx.log.debug(s"Institution Deleted: $lei")
            replyTo ! InstitutionDeleted(lei)
          }
        case Get(replyTo) =>
          replyTo ! state.institution
          Effect.none
      }
  }

  val eventHandler
    : (InstitutionState, InstitutionEvent) => (InstitutionState) = {
    case (state, InstitutionCreated(i))  => state.copy(Some(i))
    case (state, InstitutionModified(i)) => modifyInstitution(i, state)
    case (state, InstitutionDeleted(_))  => state.copy(None)
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
