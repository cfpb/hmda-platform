package hmda.publisher.scheduler.schedules

import enumeratum._

import scala.collection.immutable

sealed trait Schedule extends EnumEntry

object Schedules extends Enum[Schedule] {
  case object PanelScheduler2018        extends Schedule
  case object PanelScheduler2019        extends Schedule
  case object PanelScheduler2020        extends Schedule
  case object LarScheduler2018          extends Schedule
  case object LarPublicScheduler2018    extends Schedule
  case object LarPublicScheduler2019    extends Schedule
  case object LarScheduler2019          extends Schedule
  case object LarScheduler2020          extends Schedule
  case object LarSchedulerLoanLimit2019 extends Schedule
  case object LarSchedulerLoanLimit2020 extends Schedule
  case object TsScheduler2018           extends Schedule
  case object TsPublicScheduler2018     extends Schedule
  case object TsPublicScheduler2019     extends Schedule
  case object TsScheduler2019           extends Schedule
  case object TsScheduler2020           extends Schedule
  case object LarSchedulerQuarterly2020 extends Schedule
  case object TsSchedulerQuarterly2020  extends Schedule

  override def values: immutable.IndexedSeq[Schedule] = findValues
}