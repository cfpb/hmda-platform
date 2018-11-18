package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.edits.EditDetail
import hmda.persistence.HmdaTypedPersistentActor
import EditDetailsConverter._

trait EditDetailPersistenceCommand

case class AddEditDetail(hmdaRowValidatedError: HmdaRowValidatedError,
                         replyTo: ActorRef[EditDetailPersistenceEvent])
    extends EditDetailPersistenceCommand

case class PersistEditDetail(editDetail: EditDetail,
                             replyTo: ActorRef[EditDetailPersistenceEvent])
    extends EditDetailPersistenceCommand

trait EditDetailPersistenceEvent
case class EditDetailAdded(editDetail: EditDetail)
    extends EditDetailPersistenceEvent

case class EditDetailPersistenceState()

object EditDetailPersistence
    extends HmdaTypedPersistentActor[EditDetailPersistenceCommand,
                                     EditDetailPersistenceEvent,
                                     EditDetailPersistenceState] {

  override val name: String = "EditDetail"

  override def behavior(
      entityId: String): Behavior[EditDetailPersistenceCommand] = {
    Behaviors.setup { ctx =>
      PersistentBehavior(
        persistenceId = PersistenceId(entityId),
        emptyState = EditDetailPersistenceState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      )
    }
  }

  override def commandHandler(ctx: ActorContext[EditDetailPersistenceCommand])
    : CommandHandler[EditDetailPersistenceCommand,
                     EditDetailPersistenceEvent,
                     EditDetailPersistenceState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case AddEditDetail(hmdaRowValidatedError, replyTo) =>
        val editDetailEvents = validatedRowToEditDetails(hmdaRowValidatedError)
        editDetailEvents.foreach(e =>
          ctx.asScala.self ! PersistEditDetail(e, replyTo))
        Effect.none

      case PersistEditDetail(editDetail, replyTo) =>
        val evt = EditDetailAdded(editDetail)
        Effect.persist(evt).thenRun { _ =>
          log.debug(s"Persisted: $evt")
          replyTo ! evt
        }
    }
  }

  override def eventHandler
    : (EditDetailPersistenceState,
       EditDetailPersistenceEvent) => EditDetailPersistenceState = {
    //TODO: update state
    case (state, EditDetailAdded(e)) => state
    case (state, _)                  => state
  }

}
