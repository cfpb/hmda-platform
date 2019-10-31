package hmda.persistence.institution

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, TypedActorContext }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior, RetentionCriteria }
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.stream.ActorMaterializer
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.messages.pubsub.HmdaTopics._
import hmda.model.institution.{ Institution, InstitutionDetail }
import hmda.publication.KafkaUtils._
import hmda.persistence.HmdaTypedPersistentActor
import hmda.utils.YearUtils
import scala.concurrent.Future

object InstitutionPersistence extends HmdaTypedPersistentActor[InstitutionCommand, InstitutionEvent, InstitutionState] {

  override final val name = "Institution"

  override def behavior(entityId: String): Behavior[InstitutionCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started Institution: $entityId")
      EventSourcedBehavior[InstitutionCommand, InstitutionEvent, InstitutionState](
        persistenceId = PersistenceId(entityId),
        emptyState = InstitutionState(None),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10))
        .withTagger(_ => Set(name.toLowerCase()))
    }

  override def commandHandler(
    ctx: TypedActorContext[InstitutionCommand]
  ): CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    val log                                      = ctx.asScala.log
    implicit val system: ActorSystem             = ctx.asScala.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    (state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          if (state.institution.isEmpty) {
            Effect.persist(InstitutionCreated(i)).thenRun { _ =>
              log.debug(s"Institution Created: ${i.toString}")
              val event = InstitutionCreated(i)
              publishInstitutionEvent(i.LEI, InstitutionKafkaEvent("InstitutionCreated", event))
              replyTo ! event
            }
          } else {
            Effect.none.thenRun { _ =>
              log
                .debug(s"Institution already exists: ${i.toString}")
              replyTo ! InstitutionCreated(i)
            }
          }
        case ModifyInstitution(i, replyTo) =>
          if (state.institution
                .map(i => (i.LEI, i.activityYear))
                .contains((i.LEI, i.activityYear))) {
            Effect.persist(InstitutionModified(i)).thenRun { _ =>
              log.debug(s"Institution Modified: ${i.toString}")
              val event = InstitutionModified(i)
              publishInstitutionEvent(i.LEI, InstitutionKafkaEvent("InstitutionModified", event))
              replyTo ! event
            }
          } else {
            Effect.none.thenRun { _ =>
              log
                .warning(s"Institution with LEI: ${i.LEI} does not exist")
              replyTo ! InstitutionNotExists(i.LEI)
            }
          }
        case DeleteInstitution(lei, activityYear, replyTo) =>
          if (state.institution
                .map(i => (i.LEI, i.activityYear))
                .contains((lei, activityYear))) {
            Effect.persist(InstitutionDeleted(lei, activityYear)).thenRun { _ =>
              log.debug(s"Institution Deleted: $lei")
              val event = InstitutionDeleted(lei, activityYear)
              publishInstitutionEvent(lei, InstitutionKafkaEvent("InstitutionDeleted", event))
              replyTo ! event
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

  override val eventHandler: (InstitutionState, InstitutionEvent) => InstitutionState = {
    case (state, InstitutionCreated(i))         => state.copy(Some(i))
    case (state, InstitutionModified(i))        => modifyInstitution(i, state)
    case (state, InstitutionDeleted(lei, year)) => state.copy(None)
    case (state, evt @ FilingAdded(_))          => state.update(evt)
    case (state, InstitutionNotExists(_))       => state
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[InstitutionCommand]] =
    super.startShardRegion(sharding)

  private def publishInstitutionEvent(
    institutionID: String,
    event: InstitutionKafkaEvent
  )(implicit system: ActorSystem, materializer: ActorMaterializer): Future[Done] =
    produceInstitutionRecord(institutionTopic, institutionID, event)

  private def modifyInstitution(institution: Institution, state: InstitutionState): InstitutionState =
    if (state.isEmpty) {
      state
    } else {
      if (institution.LEI == state.institution.get.LEI) {
        state.copy(Some(institution))
      } else {
        state
      }
    }

  def selectInstitution(sharding: ClusterSharding, lei: String, year: Int): EntityRef[InstitutionCommand] =
    if (year == 2018) sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$lei")
    else sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$lei-$year")
}
