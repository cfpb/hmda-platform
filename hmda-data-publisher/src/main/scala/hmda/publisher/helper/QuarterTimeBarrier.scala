package hmda.publisher.helper

import java.time.{Clock, LocalDate}

import hmda.publisher.HmdaDataPublisherApp.log
import hmda.publisher.validation.PublishingGuard.Period
import hmda.util.BankFilterUtils.config
import hmda.util.Filer

class QuarterTimeBarrier(clock: Clock) {

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


}

object QuarterTimeBarrier {
  private val rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)

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
    }
  }

}