package hmda.persistence.filing

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef, EntityTypeKey }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior, RetentionCriteria }
import com.typesafe.scalalogging.LazyLogging
import hmda.messages.filing.FilingCommands._
import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingEvent, SubmissionAdded, SubmissionUpdated }
import hmda.messages.institution.InstitutionCommands.AddFiling
import hmda.model.filing.FilingDetails
import hmda.model.filing.submission.Signed
import hmda.persistence.HmdaTypedPersistentActor
import hmda.persistence.institution.InstitutionPersistence
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period

object FilingPersistence extends HmdaTypedPersistentActor[FilingCommand, FilingEvent, FilingState] with LazyLogging {

  override final val name = "Filing"

  val ShardingTypeName = EntityTypeKey[FilingCommand](name)

  override def behavior(filingId: String): Behavior[FilingCommand] =
    Behaviors.setup { ctx =>
      ctx.log.debug(s"Started Filing Persistence: s$filingId")
      EventSourcedBehavior[FilingCommand, FilingEvent, FilingState](
        persistenceId = PersistenceId.ofUniqueId(filingId),
        emptyState = FilingState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10))
    }

  override def commandHandler(ctx: ActorContext[FilingCommand]): CommandHandler[FilingCommand, FilingEvent, FilingState] = { (state, cmd) =>
    val log      = ctx.log
    val sharding = ClusterSharding(ctx.system)
    cmd match {
      case CreateFiling(filing, replyTo) =>
        Effect.persist(FilingCreated(filing)).thenRun { _ =>
          log.debug(s"Filing created: ${filing.lei}-${filing.period}")
          val institutionPersistence = {
            if (filing.period == "2018") {
              sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-${filing.lei}")
            } else {
              sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-${filing.lei}-${filing.period}")
            }
          }
          institutionPersistence ! AddFiling(filing, None)
          replyTo ! FilingCreated(filing)
        }

      case GetFiling(replyTo) =>
        if (state.filing.isEmpty) {
          replyTo ! None
        } else {
          replyTo ! Some(state.filing)
        }
        Effect.none

      case GetFilingDetails(replyTo) =>
        if (state.filing.isEmpty) {
          replyTo ! None
        } else {
          replyTo ! Some(FilingDetails(state.filing, state.submissions))
        }
        Effect.none

      case AddSubmission(submission, replyTo) =>
        Effect.persist(SubmissionAdded(submission)).thenRun { _ =>
          log.debug(s"Added submission: ${submission.toString}")
          replyTo match {
            case Some(ref) => ref ! submission
            case None      => Effect.none //Do not reply
          }
        }

      case UpdateSubmission(updated, replyTo) =>
        if (state.submissions.map(_.id).contains(updated.id)) {
          Effect.persist(SubmissionUpdated(updated)).thenRun { _ =>
            log.debug(s"Updated submission: ${updated.toString}")
            replyTo match {
              case Some(ref) => ref ! updated
              case None      => Effect.none //Do not reply
            }
          }
        } else {
          log.warn(s"Could not update submission wth $updated")
          Effect.none
        }

      case GetLatestSubmission(replyTo) =>
        val maybeSubmission = state.submissions
          .sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
          .headOption
        replyTo ! maybeSubmission
        Effect.none

      case GetSubmissionSummary(submissionId, replyTo) =>
        val maybeSubmission = state.submissions
          .filter(_.id.sequenceNumber == submissionId.sequenceNumber)
          .headOption
        replyTo ! maybeSubmission
        Effect.none

      case GetSubmissions(replyTo) =>
        replyTo ! state.submissions
        Effect.none

      //This method will be similar to GetLatestSubmission but will fetch the one with highest sequencenumber and status of Signed (15)
      case GetLatestSignedSubmission(replyTo) =>
        val maybeSignedSubmission = state.submissions
          .filter(_.status == Signed)
          .sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
          .headOption
        replyTo ! maybeSignedSubmission
        Effect.none

      //This method will be similar to GetLatestSubmission but will fetch the one with highest sequencenumber and status of Signed (15)
      case GetOldestSignedSubmission(replyTo) =>
        println("inside oldest")
        println(state.submissions)
        println(state.submissions.size)
        val maybeSignedSubmission = state.submissions
          .filter(_.status == Signed)
          .sortWith(_.id.sequenceNumber < _.id.sequenceNumber)
          .headOption
        replyTo ! maybeSignedSubmission
        Effect.none

      case FilingStop() =>
        Effect.stop()

      case _ =>
        Effect.unhandled
    }
  }

  val eventHandler: (FilingState, FilingEvent) => FilingState = {
    case (state, evt @ SubmissionAdded(_))   => state.update(evt)
    case (state, evt @ FilingCreated(_))     => state.update(evt)
    case (state, evt @ SubmissionUpdated(_)) => state.update(evt)
    case (state, _)                          => state
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[FilingCommand]] =
    super.startShardRegion(sharding, FilingStop())

  case class EntityId(lei: String, period: Period) {
    def mkString: String = makeEntityId(lei, period.year, period.quarter)
  }

  def parseEntityId(entityId: String): Option[EntityId] = {
    val prefix = s"${FilingPersistence.name}-"
    if (entityId.startsWith(prefix)) {
      try {
        val Array(lei, periodStr) = entityId.stripPrefix(prefix).split("-", 2)
        val period                = YearUtils.parsePeriod(periodStr).toTry.get
        Some(EntityId(lei, period))
      } catch {
        case err: Throwable =>
          logger.error(s"Failed to parse filling entity id: ${entityId}", err)
          None
      }
    } else {
      None
    }
  }

  private def makeEntityId(lei: String, year: Int, quarter: Option[String]) = {
    val period = YearUtils.period(year, quarter)
    s"${FilingPersistence.name}-$lei-$period"
  }

  def selectFiling(sharding: ClusterSharding, lei: String, year: Int, quarter: Option[String]): EntityRef[FilingCommand] = {
    sharding.entityRefFor(FilingPersistence.typeKey, makeEntityId(lei, year, quarter))
  }

}