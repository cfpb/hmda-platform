package hmda.publisher.util

import java.time.Instant

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import hmda.publisher.scheduler.schedules.{ Schedule, Schedules }
import hmda.publisher.util.PublishingReporter.Command
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted.Status
import hmda.publisher.util.PublishingReporter.ScheduleGroupId.forSchedule
import hmda.publisher.util.PublishingReporter.State.AddResult

import scala.concurrent.duration.FiniteDuration
// $COVERAGE-OFF$
class PublishingReporter(context: ActorContext[PublishingReporter.Command], reporter: MattermostNotifier, groupTimeout: FiniteDuration)(
  state: PublishingReporter.State
) extends AbstractBehavior[PublishingReporter.Command](context) {
  override def onMessage(msg: PublishingReporter.Command): Behavior[PublishingReporter.Command] = msg match {
    case Command.GroupTimedOut(id) =>
      state.groups.get(id) match {
        case Some(group) =>
          val msgBase      = prepareMessage(group.entries)
          val expectations = group.remaining.map(x => s"(schedule=${x.schedule}, numOfFiles=${x.numOfFiles})")
          val msg =
            msgBase + s"\nTimed out when waiting for this schedule group ${id}. Missing files: ${expectations.mkString(", ")}"
          reporter.report(msg)
          updateState(state.emptyGroup(id))
        case None =>
          // group finished reporting and was handled before timeout occurred
          this
      }

    case filePublished: Command.FilePublishingCompleted =>
      state.add(filePublished) match {
        case AddResult.NewGroup(newState, groupId) =>
          context.scheduleOnce(groupTimeout, context.self, PublishingReporter.Command.GroupTimedOut(groupId))
          updateState(newState)
        case AddResult.NotYetFull(newState) =>
          updateState(newState)
        case AddResult.FullAndEmptied(newState, entries) =>
          val msg = prepareMessage(entries)
          reporter.report(msg)
          updateState(newState)
      }
  }

  def prepareMessage(entries: List[FilePublishingCompleted]): String =
    entries
      .map(e =>
        e.status match {
          case Status.Success => s"Pushed ${e.fileName} to S3 at ${e.time} with ${e.numOfRecords.getOrElse("?")} rows"
          case Status.Error(message) =>
            s"Pushing ${e.fileName} to S3 at ${e.time} with ${e.numOfRecords.getOrElse("?")} rows failed with message: ${message}"
        }
      )
      .mkString("\n")

  def updateState(newState: PublishingReporter.State) = new PublishingReporter(context, reporter, groupTimeout)(newState)
}

object PublishingReporter {

  sealed trait Command
  object Command {
    private[PublishingReporter] case class GroupTimedOut(id: ScheduleGroupId) extends Command
    case class FilePublishingCompleted(
                                        schedule: Schedule,
                                        fileName: String,
                                        numOfRecords: Option[Int],
                                        time: Instant,
                                        status: FilePublishingCompleted.Status
                                      ) extends Command {
      def groupId: ScheduleGroupId = ScheduleGroupId.forSchedule(schedule)
    }
    object FilePublishingCompleted {
      sealed trait Status
      object Status {
        case object Success               extends Status
        case class Error(message: String) extends Status
      }
    }
  }

  case class State private (groups: Map[ScheduleGroupId, State.Group]) {
    def add(f: FilePublishingCompleted): State.AddResult = {
      val id = f.groupId
      val group = groups
        .getOrElse(id, State.Group.newForId(id))
        .add(f)
      val isNew = !groups.contains(id)
      if (group.isFull) {
        State.AddResult.FullAndEmptied(this.copy(groups = groups - id), group.entries)
      } else {
        if (isNew) State.AddResult.NewGroup(this.copy(groups = groups.updated(id, group)), id)
        else State.AddResult.NotYetFull(this.copy(groups = groups.updated(id, group)))
      }
    }
    def isFull(groupId: ScheduleGroupId): Boolean   = groups.get(groupId).exists(_.isFull)
    def emptyGroup(groupId: ScheduleGroupId): State = this.copy(groups = groups - groupId)
  }

  object State {
    val empty: State = State(Map())

    sealed trait AddResult
    object AddResult {
      case class NewGroup(newState: State, groupId: ScheduleGroupId)                     extends AddResult
      case class NotYetFull(newState: State)                                             extends AddResult
      case class FullAndEmptied(newState: State, entries: List[FilePublishingCompleted]) extends AddResult
    }

    case class Expectation(schedule: Schedule, numOfFiles: Int) {
      def isSatisfied: Boolean = numOfFiles == 0
      def added(s: Schedule)   = if (schedule == s) copy(numOfFiles = numOfFiles - 1) else this
    }

    object Expectation {
      def baseForSchedule(s: Schedule): Expectation = {
        val numOfFiles = s match {
          case _ => 1
        }
        Expectation(s, numOfFiles)
      }
    }

    case class Group private (entries: List[FilePublishingCompleted], remaining: List[Expectation]) {
      def add(f: FilePublishingCompleted): Group = {
        val newEntries   = entries :+ f
        val newRemaining = remaining.map(_.added(f.schedule)).filterNot(_.isSatisfied)
        this.copy(entries = newEntries, remaining = newRemaining)
      }
      def isFull: Boolean = remaining.isEmpty
    }
    object Group {
      val expectations: Map[ScheduleGroupId, List[Expectation]] =
        Schedules.values
          .groupBy(forSchedule)
          .view.mapValues(_.map(Expectation.baseForSchedule).toList)
          .toMap
          .withDefault(_ => List())
      def newForId(id: ScheduleGroupId): Group =
        Group(List(), expectations(id))
    }
  }

  sealed trait ScheduleGroupId
  object ScheduleGroupId {
    case object PanelScheduler2018Id          extends ScheduleGroupId
    case object PanelScheduler2019Id          extends ScheduleGroupId
    case object PanelScheduler2020Id          extends ScheduleGroupId
    case object PanelScheduler2021Id          extends ScheduleGroupId
    case object PanelScheduler2022Id          extends ScheduleGroupId


    case object LarScheduler2018Id            extends ScheduleGroupId
    case object LarPublicScheduler2018Id      extends ScheduleGroupId
    case object LarPublicScheduler2019Id          extends ScheduleGroupId
    case object LarPublicScheduler2020Id          extends ScheduleGroupId
    case object LarPublicScheduler2021Id          extends ScheduleGroupId
    case object LarScheduler2019Id          extends ScheduleGroupId
    case object LarScheduler2020Id          extends ScheduleGroupId
    case object LarScheduler2021Id          extends ScheduleGroupId
    case object LarScheduler2022Id          extends ScheduleGroupId


    case object LarSchedulerLoanLimit2019Id extends ScheduleGroupId
    case object LarSchedulerLoanLimit2020Id extends ScheduleGroupId
    case object LarSchedulerLoanLimit2021Id extends ScheduleGroupId
    case object LarSchedulerLoanLimit2022Id extends ScheduleGroupId

    case object TsScheduler2018Id          extends ScheduleGroupId
    case object TsPublicScheduler2018Id          extends ScheduleGroupId
    case object TsPublicScheduler2019Id          extends ScheduleGroupId
    case object TsPublicScheduler2020Id          extends ScheduleGroupId
    case object TsPublicScheduler2021Id          extends ScheduleGroupId
    case object TsScheduler2019Id extends ScheduleGroupId
    case object TsScheduler2020Id extends ScheduleGroupId
    case object TsScheduler2021Id extends ScheduleGroupId
    case object TsScheduler2022Id extends ScheduleGroupId


    case object LarSchedulerQuarterly2020Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2020Id          extends ScheduleGroupId
    case object LarSchedulerQuarterly2021Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2021Id extends ScheduleGroupId

    case object LarSchedulerQuarterly2022Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2022Id extends ScheduleGroupId

    case object LarSchedulerQuarterly2023Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2023Id extends ScheduleGroupId

    case object PanelScheduleId extends ScheduleGroupId
    case object LarPublicScheduleId extends ScheduleGroupId
    case object CombinedMLarPublicScheduleId extends ScheduleGroupId
    case object LarScheduleId extends ScheduleGroupId
    case object LarLoanLimitScheduleId extends ScheduleGroupId
    case object TsPublicScheduleId extends ScheduleGroupId
    case object TsScheduleId extends ScheduleGroupId
    case object LarQuarterlyScheduleId extends ScheduleGroupId
    case object TsQuarterlyScheduleId extends ScheduleGroupId

    def forSchedule(s: Schedule): ScheduleGroupId = s match {
      case Schedules.PanelSchedule => PanelScheduleId
      case Schedules.LarPublicSchedule => LarPublicScheduleId
      case Schedules.CombinedMLarPublicSchedule => CombinedMLarPublicScheduleId
      case Schedules.LarSchedule => LarScheduleId
      case Schedules.LarLoanLimitSchedule => LarLoanLimitScheduleId
      case Schedules.TsPublicSchedule => TsPublicScheduleId
      case Schedules.TsSchedule => TsScheduleId
      case Schedules.LarQuarterlySchedule => LarQuarterlyScheduleId
      case Schedules.TsQuarterlySchedule => TsQuarterlyScheduleId
    }
  }

  def apply(msgReporter: MattermostNotifier, groupTimeout: FiniteDuration): Behavior[Command] =
    Behaviors.setup(context => new PublishingReporter(context, msgReporter, groupTimeout)(State.empty))
}
// $COVERAGE-ON$