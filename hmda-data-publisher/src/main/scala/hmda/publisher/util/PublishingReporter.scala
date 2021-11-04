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
          case Schedules.PanelScheduler2018        => 1
          case Schedules.PanelScheduler2019        => 1
          case Schedules.PanelScheduler2020        => 1
          case Schedules.PanelScheduler2021        => 1
          case Schedules.LarScheduler2018          => 1
          case Schedules.LarPublicScheduler2018    => 1
          case Schedules.LarPublicScheduler2019    => 1
          case Schedules.LarPublicScheduler2020    => 1
          case Schedules.LarScheduler2019          => 1
          case Schedules.LarScheduler2020          => 1
          case Schedules.LarScheduler2021          => 1
          case Schedules.LarSchedulerLoanLimit2019 => 1
          case Schedules.LarSchedulerLoanLimit2020 => 1
          case Schedules.LarSchedulerLoanLimit2021 => 1
          case Schedules.TsScheduler2018           => 1
          case Schedules.TsPublicScheduler2018     => 1
          case Schedules.TsPublicScheduler2019     => 1
          case Schedules.TsPublicScheduler2020    => 1
          case Schedules.TsScheduler2019           => 1
          case Schedules.TsScheduler2020           => 1
          case Schedules.TsScheduler2021           => 1
          case Schedules.LarSchedulerQuarterly2020 => 1
          case Schedules.TsSchedulerQuarterly2020  => 1
          case Schedules.LarSchedulerQuarterly2021 => 1
          case Schedules.TsSchedulerQuarterly2021  => 1
          case Schedules.LarSchedulerQuarterly2022 => 1
          case Schedules.TsSchedulerQuarterly2022  => 1
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
          .mapValues(_.map(Expectation.baseForSchedule).toList)
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

    case object LarScheduler2018Id            extends ScheduleGroupId
    case object LarPublicScheduler2018Id      extends ScheduleGroupId
    case object LarPublicScheduler2019Id          extends ScheduleGroupId
    case object LarPublicScheduler2020Id          extends ScheduleGroupId
    case object LarScheduler2019Id          extends ScheduleGroupId
    case object LarScheduler2020Id          extends ScheduleGroupId
    case object LarScheduler2021Id          extends ScheduleGroupId

    case object LarSchedulerLoanLimit2019Id extends ScheduleGroupId
    case object LarSchedulerLoanLimit2020Id extends ScheduleGroupId
    case object LarSchedulerLoanLimit2021Id extends ScheduleGroupId

    case object TsScheduler2018Id          extends ScheduleGroupId
    case object TsPublicScheduler2018Id          extends ScheduleGroupId
    case object TsPublicScheduler2019Id          extends ScheduleGroupId
    case object TsPublicScheduler2020Id          extends ScheduleGroupId
    case object TsScheduler2019Id extends ScheduleGroupId
    case object TsScheduler2020Id extends ScheduleGroupId
    case object TsScheduler2021Id extends ScheduleGroupId

    case object LarSchedulerQuarterly2020Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2020Id          extends ScheduleGroupId
    case object LarSchedulerQuarterly2021Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2021Id extends ScheduleGroupId

    case object LarSchedulerQuarterly2022Id          extends ScheduleGroupId
    case object TsSchedulerQuarterly2022Id extends ScheduleGroupId


    def forSchedule(s: Schedule): ScheduleGroupId = s match {

      case Schedules.PanelScheduler2018        => PanelScheduler2018Id
      case Schedules.PanelScheduler2019        => PanelScheduler2019Id
      case Schedules.PanelScheduler2020        => PanelScheduler2020Id
      case Schedules.PanelScheduler2021        => PanelScheduler2021Id

      case Schedules.LarScheduler2018          => LarScheduler2018Id
      case Schedules.LarPublicScheduler2018    => LarPublicScheduler2018Id

      case Schedules.LarPublicScheduler2019    => LarPublicScheduler2019Id
      case Schedules.LarPublicScheduler2020   => LarPublicScheduler2020Id
      case Schedules.LarScheduler2019          => LarScheduler2019Id
      case Schedules.LarScheduler2020          => LarScheduler2020Id
      case Schedules.LarScheduler2021          => LarScheduler2021Id

      case Schedules.LarSchedulerLoanLimit2019 => LarSchedulerLoanLimit2019Id
      case Schedules.LarSchedulerLoanLimit2020 => LarSchedulerLoanLimit2020Id
      case Schedules.LarSchedulerLoanLimit2021 => LarSchedulerLoanLimit2021Id


      case Schedules.TsScheduler2018           => TsScheduler2018Id
      case Schedules.TsPublicScheduler2018     => TsPublicScheduler2018Id
      case Schedules.TsPublicScheduler2019     => TsPublicScheduler2019Id
      case Schedules.TsPublicScheduler2020     => TsPublicScheduler2020Id
      case Schedules.TsScheduler2019           => TsScheduler2019Id
      case Schedules.TsScheduler2020           => TsScheduler2020Id
      case Schedules.TsScheduler2021           => TsScheduler2021Id

      case Schedules.LarSchedulerQuarterly2020 => LarSchedulerQuarterly2020Id
      case Schedules.TsSchedulerQuarterly2020  => TsSchedulerQuarterly2020Id
      case Schedules.LarSchedulerQuarterly2021 => LarSchedulerQuarterly2021Id
      case Schedules.TsSchedulerQuarterly2021  => TsSchedulerQuarterly2021Id
      case Schedules.LarSchedulerQuarterly2022 => LarSchedulerQuarterly2022Id
      case Schedules.TsSchedulerQuarterly2022  => TsSchedulerQuarterly2022Id
    }
  }

  def apply(msgReporter: MattermostNotifier, groupTimeout: FiniteDuration): Behavior[Command] =
    Behaviors.setup(context => new PublishingReporter(context, msgReporter, groupTimeout)(State.empty))
}
// $COVERAGE-ON$