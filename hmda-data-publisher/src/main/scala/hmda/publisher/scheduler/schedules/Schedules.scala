package hmda.publisher.scheduler.schedules

import enumeratum._

import scala.collection.immutable

sealed trait Schedule extends EnumEntry

object Schedules extends Enum[Schedule] {
  case object PanelSchedule extends Schedule
  case object LarPublicSchedule extends Schedule
  case object CombinedMLarPublicSchedule extends Schedule
  case object LarSchedule extends Schedule
  case object LarLoanLimitSchedule extends Schedule
  case object TsPublicSchedule extends Schedule
  case object TsSchedule extends Schedule
  case object LarQuarterlySchedule extends Schedule
  case object TsQuarterlySchedule extends Schedule
  override def values: immutable.IndexedSeq[Schedule] = findValues
}

sealed case class ScheduleWithYear(schedule: Schedule, year: Int)