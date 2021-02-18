package hmda.persistence.institution

import akka.Done
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import akka.stream.Materializer
import com.typesafe.config.Config
import hmda.HmdaPlatform.{institutionKafkaProducer, log}
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.messages.pubsub.HmdaTopics._
import hmda.model.institution.{Institution, InstitutionDetail}
import hmda.persistence.HmdaTypedPersistentActor
import hmda.publication.KafkaUtils._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object InstitutionPersistence extends HmdaTypedPersistentActor[InstitutionCommand, InstitutionEvent, InstitutionState] {
  override final val name = "Institution"

  override def behavior(entityId: String): Behavior[InstitutionCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started Institution: $entityId")
      EventSourcedBehavior[InstitutionCommand, InstitutionEvent, InstitutionState](
        persistenceId = PersistenceId.ofUniqueId(entityId),
        emptyState = InstitutionState(None),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10))
        .withTagger(_ => Set(name.toLowerCase()))
    }

  override def commandHandler(
                               ctx: ActorContext[InstitutionCommand]
                             ): CommandHandler[InstitutionCommand, InstitutionEvent, InstitutionState] = {
    val log                                 = ctx.log
    implicit val system: ActorSystem[_]     = ctx.system
    implicit val materializer: Materializer = Materializer(ctx)
    val config                              = system.settings.config
    val louConfig = ConfigFactory.load("application.conf").getConfig("filter")
    val louList =
      louConfig.getString("lou-filter-list").toUpperCase.split(",")

    (state, cmd) =>
      cmd match {
        case CreateInstitution(i, replyTo) =>
          if (louList.contains(i.LEI)) {
            Effect.none.thenRun { _ =>
              log.warn(s"Institution Creation with LOU attempted: ${i.toString}")
              replyTo ! InstitutionWithLou(i)
            }
          }
          else if (state.institution.isEmpty) {
            Effect.persist(InstitutionCreated(i)).thenRun { _ =>
              log.debug(s"Institution Created: ${i.toString}")
              val event = InstitutionCreated(i)
              publishInstitutionEvent(i.LEI, InstitutionKafkaEvent("InstitutionCreated", event), config)
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
              publishInstitutionEvent(i.LEI, InstitutionKafkaEvent("InstitutionModified", event), config)
              replyTo ! event
            }
          } else {
            Effect.none.thenRun { _ =>
              log.warn(s"Institution with LEI: ${i.LEI} does not exist")
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
              publishInstitutionEvent(lei, InstitutionKafkaEvent("InstitutionDeleted", event), config)
              replyTo ! event
            }
          } else {
            Effect.none.thenRun { _ =>
              log.warn(s"Institution with LEI: $lei does not exist")
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
          log.info(s"Stopping ${ctx.asScala.self.path.name}")
          Effect.stop()
      }
  }

  override val eventHandler: (InstitutionState, InstitutionEvent) => InstitutionState = {
    case (state, InstitutionCreated(i))         => state.copy(Some(i))
    case (state, InstitutionWithLou(i))         => state.copy(Some(i))
    case (state, InstitutionModified(i))        => modifyInstitution(i, state)
    case (state, InstitutionDeleted(lei, year)) => state.copy(None)
    case (state, evt @ FilingAdded(_))          => state.update(evt)
    case (state, InstitutionNotExists(_))       => state
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[InstitutionCommand]] =
    super.startShardRegion(sharding, InstitutionStop)

  private def publishInstitutionEvent(
                                       institutionID: String,
                                       event: InstitutionKafkaEvent,
                                       config: Config
                                     )(implicit system: ActorSystem[_], materializer: Materializer): Future[Done] =
    produceInstitutionRecord(institutionTopic, institutionID, event, institutionKafkaProducer)

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

  case class EntityId(lei: String, year: Int) {
    def mkString = makeEntityId(lei, year)
  }

  def parseEntityId(entityIdStr: String): Option[EntityId] = {
    val prefix = s"${InstitutionPersistence.name}-"
    if(entityIdStr.startsWith(prefix)) {
      val parts = entityIdStr.stripPrefix(prefix).split("-")
      if(parts.size == 1) Some(EntityId(parts.head, 2018))
      else if(parts.size == 2) Some(EntityId(parts(0), parts(1).toInt))
      else {
        log.error(s"Cant parse institution entity id: ${entityIdStr}")
        None
      }
    } else {
      None
    }
  }
  def makeEntityId(lei: String, year: Int) = {
    if (year == 2018) s"${InstitutionPersistence.name}-$lei"
    else s"${InstitutionPersistence.name}-$lei-$year"
  }

  def selectInstitution(sharding: ClusterSharding, lei: String, year: Int): EntityRef[InstitutionCommand] =
    sharding.entityRefFor(InstitutionPersistence.typeKey, makeEntityId(lei, year))
}