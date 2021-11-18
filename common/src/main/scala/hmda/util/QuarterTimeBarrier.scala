package hmda.util

import java.time.{Clock, LocalDate}
import hmda.util.BankFilterUtils.config
import hmda.utils.YearUtils.Period

import java.time.format.DateTimeFormatter

object QuarterTimeBarrier {
  private val rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)

  import java.time.format.DateTimeFormatter

  val formatter: DateTimeFormatter =DateTimeFormatter.BASIC_ISO_DATE

  def getEndDateForQuarter(quarter: Period): LocalDate = {
    quarter match {
      case Period(2020, Some("Q1")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q1.endDayOfYear)
      case Period(2020, Some("Q2")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q2.endDayOfYear)
      case Period(2020, Some("Q3")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q3.endDayOfYear)
      case Period(2021, Some("Q1")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q1.endDayOfYear)
      case Period(2021, Some("Q2")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q2.endDayOfYear)
      case Period(2021, Some("Q3")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q3.endDayOfYear)
      case Period(2022, Some("Q1")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q1.endDayOfYear)
      case Period(2022, Some("Q2")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q2.endDayOfYear)
      case Period(2022, Some("Q3")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q3.endDayOfYear)
    }
  }

  def getFirstDateForQuarter(quarter: Period): LocalDate = {
    quarter match {
      case Period(2020, Some("Q1")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q1.startDayOfYear)
      case Period(2020, Some("Q2")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q2.startDayOfYear)
      case Period(2020, Some("Q3")) => LocalDate.ofYearDay(2020,rulesConfig.qf.q3.startDayOfYear)
      case Period(2021, Some("Q1")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q1.startDayOfYear)
      case Period(2021, Some("Q2")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q2.startDayOfYear)
      case Period(2021, Some("Q3")) => LocalDate.ofYearDay(2021,rulesConfig.qf.q3.startDayOfYear)
      case Period(2022, Some("Q1")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q1.startDayOfYear)
      case Period(2022, Some("Q2")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q2.startDayOfYear)
      case Period(2022, Some("Q3")) => LocalDate.ofYearDay(2022,rulesConfig.qf.q3.startDayOfYear)
    }
  }
  def actionTakenWithinQuarter(actionTakenDate: Int, period: Period ): Boolean= {
    val actionTakenDateLocal = LocalDate.parse(actionTakenDate.toString,formatter)

    (actionTakenDateLocal.isAfter(QuarterTimeBarrier.getFirstDateForQuarter(period)) &&
      actionTakenDateLocal.isBefore(QuarterTimeBarrier.getEndDateForQuarter(period))) ||
      actionTakenDateLocal.isEqual(QuarterTimeBarrier.getFirstDateForQuarter(period)) ||
      actionTakenDateLocal.isEqual(QuarterTimeBarrier.getEndDateForQuarter(period))
  }


}