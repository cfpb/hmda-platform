package hmda.publisher.helper

import hmda.publisher.query.component.YearPeriod

import java.time.{ Clock, LocalDate }
import hmda.publisher.validation.PublishingGuard.Period
import hmda.util.BankFilterUtils.config
import hmda.util.Filer

class QuarterTimeBarrier(clock: Clock) {
  import QuarterTimeBarrier._

  def runIfStillRelevant[T](quarter: Period.Quarter)(thunk: => T): Option[T] = {
    val now = LocalDate.now(clock)
    if ((now.isAfter(QuarterTimeBarrier.getFirstDateForQuarter(quarter)) &&
      now.isBefore(QuarterTimeBarrier.getEndDateForQuarter(quarter).plusDays(8)))||
      now.isEqual(QuarterTimeBarrier.getFirstDateForQuarter(quarter))) {
      Some(thunk)
    } else {
      None
    }
  }

  implicit private class ExtendedLocalDate(date: LocalDate) {
    def isOnOrBefore(compareDate: LocalDate): Boolean = date.isBefore(compareDate) || date.isEqual(compareDate)
    def isOnOrAfter(compareDate: LocalDate): Boolean = date.isAfter(compareDate) || date.isEqual(compareDate)
    def isBetween(start: LocalDate, end: LocalDate): Boolean = date.isOnOrAfter(start) && date.isOnOrBefore(end)
  }

  def runIfStillRelevant[T](year: Int, period: YearPeriod)(thunk: => T): Option[T] = {
    val now = LocalDate.now(clock)
    if (now.isBetween(
      getStartDateForQuarter(year, period),
      getEndDateForQuarter(year, period).plusDays(8))
    ) {
      Some(thunk)
    } else {
      None
    }
  }

  def setRulesConfigWithActualYear(actualYear: Int): Unit = {
    QuarterTimeBarrier.setRulesConfigWithActualYear(actualYear)
  }


}

object QuarterTimeBarrier {
  private var rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)

  def getEndDateForQuarter(quarter: Period.Quarter): LocalDate = {
    quarter match {
      case Period.y2020Q1 => LocalDate.ofYearDay(2020,rulesConfig.qf.q1.endDayOfYear)
      case Period.y2020Q2 => LocalDate.ofYearDay(2020,rulesConfig.qf.q2.endDayOfYear)
      case Period.y2020Q3 => LocalDate.ofYearDay(2020,rulesConfig.qf.q3.endDayOfYear)
      case Period.y2021Q1 => LocalDate.ofYearDay(2021,rulesConfig.qf.q1.endDayOfYear)
      case Period.y2021Q2 => LocalDate.ofYearDay(2021,rulesConfig.qf.q2.endDayOfYear)
      case Period.y2021Q3 => LocalDate.ofYearDay(2021,rulesConfig.qf.q3.endDayOfYear)
      case Period.y2022Q1 => LocalDate.ofYearDay(2022,rulesConfig.qf.q1.endDayOfYear)
      case Period.y2022Q2 => LocalDate.ofYearDay(2022,rulesConfig.qf.q2.endDayOfYear)
      case Period.y2022Q3 => LocalDate.ofYearDay(2022,rulesConfig.qf.q3.endDayOfYear)
      case Period.y2023Q1 => LocalDate.ofYearDay(2023, rulesConfig.qf.q1.endDayOfYear)
      case Period.y2023Q2 => LocalDate.ofYearDay(2023, rulesConfig.qf.q2.endDayOfYear)
      case Period.y2023Q3 => LocalDate.ofYearDay(2023, rulesConfig.qf.q3.endDayOfYear)
    }
  }

  def getFirstDateForQuarter(quarter: Period.Quarter): LocalDate = {
    quarter match {
      case Period.y2020Q1 => LocalDate.ofYearDay(2020,rulesConfig.qf.q1.startDayOfYear)
      case Period.y2020Q2 => LocalDate.ofYearDay(2020,rulesConfig.qf.q2.startDayOfYear)
      case Period.y2020Q3 => LocalDate.ofYearDay(2020,rulesConfig.qf.q3.startDayOfYear)
      case Period.y2021Q1 => LocalDate.ofYearDay(2021,rulesConfig.qf.q1.startDayOfYear)
      case Period.y2021Q2 => LocalDate.ofYearDay(2021,rulesConfig.qf.q2.startDayOfYear)
      case Period.y2021Q3 => LocalDate.ofYearDay(2021,rulesConfig.qf.q3.startDayOfYear)
      case Period.y2022Q1 => LocalDate.ofYearDay(2022,rulesConfig.qf.q1.startDayOfYear)
      case Period.y2022Q2 => LocalDate.ofYearDay(2022,rulesConfig.qf.q2.startDayOfYear)
      case Period.y2022Q3 => LocalDate.ofYearDay(2022,rulesConfig.qf.q3.startDayOfYear)
      case Period.y2023Q1 => LocalDate.ofYearDay(2023, rulesConfig.qf.q1.startDayOfYear)
      case Period.y2023Q2 => LocalDate.ofYearDay(2023, rulesConfig.qf.q2.startDayOfYear)
      case Period.y2023Q3 => LocalDate.ofYearDay(2023, rulesConfig.qf.q3.startDayOfYear)
    }
  }

  def getStartDateForQuarter(year: Int, quarter: YearPeriod): LocalDate = {
    val dayOfYear = quarter match {
      case YearPeriod.Q1 => rulesConfig.qf.q1.startDayOfYear
      case YearPeriod.Q2 => rulesConfig.qf.q2.startDayOfYear
      case YearPeriod.Q3 => rulesConfig.qf.q3.startDayOfYear
      case _ => throw new IllegalArgumentException(s"Invalid quarter $quarter")
    }
    LocalDate.ofYearDay(year, dayOfYear)
  }

  def getEndDateForQuarter(year: Int, quarter: YearPeriod): LocalDate = {
    val dayOfYear = quarter match {
      case YearPeriod.Q1 => rulesConfig.qf.q1.endDayOfYear
      case YearPeriod.Q2 => rulesConfig.qf.q2.endDayOfYear
      case YearPeriod.Q3 => rulesConfig.qf.q3.endDayOfYear
      case _ => throw new IllegalArgumentException(s"Invalid quarter $quarter")
    }
    LocalDate.ofYearDay(year, dayOfYear)
  }

  def setRulesConfigWithActualYear(actualYear: Int): Unit = {
    rulesConfig = Filer.parse(config, actualYear).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)
  }

}