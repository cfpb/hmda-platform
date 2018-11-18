package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.model.edits.EditDetail
import hmda.persistence.HmdaTypedPersistentActor
import hmda.model.filing.submission.SubmissionId
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

import scala.concurrent.Future

trait EditDetailPersistenceCommand

case class PersistEditDetail(
    editDetail: EditDetail,
    replyTo: Option[ActorRef[EditDetailPersistenceEvent]])
    extends EditDetailPersistenceCommand

case class GetEditRowCount(editName: String, replyTo: ActorRef[Int])
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
    totalErrorMap: Map[String, Int] = Map.empty) {
  def update(evt: EditDetailPersistenceEvent): EditDetailPersistenceState = {
    evt match {
      case EditDetailAdded(editDetail) =>
        val editName = editDetail.edit
        val rowCount = editDetail.rows.size
        val editCount = totalErrorMap.getOrElse(editName, 0) + rowCount
        EditDetailPersistenceState(totalErrorMap.updated(editName, editCount))
      case _ => this
    }
  }
}

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
      case PersistEditDetail(editDetail, maybeReplyTo) =>
        val evt = EditDetailAdded(editDetail)
        Effect.persist(evt).thenRun { _ =>
          log.info(s"Persisted: $evt")
          maybeReplyTo match {
            case Some(replyTo) =>
              replyTo ! evt
            case None => //Do nothing
          }
        }

      case GetEditRowCount(editName, replyTo) =>
        replyTo ! state.totalErrorMap.getOrElse(editName, 0)
        Effect.none
    }
  }

  override def eventHandler
    : (EditDetailPersistenceState,
       EditDetailPersistenceEvent) => EditDetailPersistenceState = {
    case (state, evt @ EditDetailAdded(_)) => state.update(evt)
    case (state, _)                        => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[EditDetailPersistenceCommand]] = {
    super.startShardRegion(sharding)
  }

}
