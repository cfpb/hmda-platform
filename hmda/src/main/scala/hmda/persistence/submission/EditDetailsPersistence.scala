package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.model.edits.EditDetail
import hmda.persistence.HmdaTypedPersistentActor
import akka.actor
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.submissions.PaginatedResource
import akka.actor.typed.scaladsl.adapter._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import hmda.query.HmdaQuery._

import scala.concurrent.Future

trait EditDetailPersistenceCommand

case class PersistEditDetail(editDetail: EditDetail,
                             replyTo: ActorRef[EditDetailPersistenceEvent])
    extends EditDetailPersistenceCommand

case class GetEditDetails(submissionId: SubmissionId,
                          editName: String,
                          page: Int,
                          replyTo: ActorRef[Future[Seq[EditDetail]]])
    extends EditDetailPersistenceCommand

trait EditDetailPersistenceEvent

case class EditDetailAdded(editDetail: EditDetail)
    extends EditDetailPersistenceEvent

case class EditDetailPersistenceState(
    totalErrorMap: Map[String, Int] = Map.empty)

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
      case PersistEditDetail(editDetail, replyTo) =>
        val evt = EditDetailAdded(editDetail)
        Effect.persist(evt).thenRun { _ =>
          log.debug(s"Persisted: $evt")
          replyTo ! evt
        }

      case GetEditDetails(submissionId, editName, page, replyTo) =>
        implicit val untypedSystem: actor.ActorSystem =
          ctx.asScala.system.toUntyped
        implicit val materializer: ActorMaterializer = ActorMaterializer()
        val totalRowCount = state.totalErrorMap.getOrElse(editName, 0)
        val p = PaginatedResource(totalRowCount)(page)
        val persistenceId = s"${EditDetailPersistence.name}-$submissionId"
        val editDetailSource = eventEnvelopeByPersistenceId(persistenceId)
          .drop(p.fromIndex)
          .take(p.toIndex)
          .map(envelope =>
            envelope.event.asInstanceOf[EditDetailPersistenceEvent])
          .collect {
            case EditDetailAdded(editDetail) => editDetail
          }
        replyTo ! editDetailSource.runWith(Sink.seq)
        Effect.none
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
