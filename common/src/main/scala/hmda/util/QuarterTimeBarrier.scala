package hmda.util

import java.time.{Clock, LocalDate}
import hmda.util.BankFilterUtils.config
import hmda.utils.YearUtils.Period

import java.time.format.DateTimeFormatter

object QuarterTimeBarrier {
  private  val rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)

  // Builtin time format of BASIC_ISO_DATE yyyyMMdd i.e. '20111203'.
  private val formatter: DateTimeFormatter =DateTimeFormatter.BASIC_ISO_DATE
  private val  q12020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q1.endDayOfYear)
  private val  q22020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q2.endDayOfYear)
  private val  q32020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q3.endDayOfYear)

  private val  q12021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q1.endDayOfYear)
  private val  q22021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q2.endDayOfYear)
  private val  q32021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q3.endDayOfYear)

  private val  q12022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q1.endDayOfYear)
  private val  q22022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q2.endDayOfYear)
  private val  q32022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q3.endDayOfYear)

  private val  q12020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q1.startDayOfYear)
  private val  q22020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q2.startDayOfYear)
  private val  q32020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q3.startDayOfYear)

  private val  q12021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q1.startDayOfYear)
  private val  q22021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q2.startDayOfYear)
  private val  q32021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q3.startDayOfYear)

  private val  q12022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q1.startDayOfYear)
  private val  q22022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q2.startDayOfYear)
  private val  q32022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q3.startDayOfYear)

  implicit private class ExtendedLocalDate(date: LocalDate) {
    def isOnOrBefore(compareDate: LocalDate): Boolean = date.isBefore(compareDate) || date.isEqual(compareDate)
    def isBetween(start: LocalDate, end: LocalDate): Boolean = date.isAfter(start) && date.isOnOrBefore(end)
  }
  
  def actionTakenInQuarterRange(actionTakenDate: Int, period: Period):Boolean= {
    val actionTakenDateLocal = LocalDate.parse(actionTakenDate.toString, formatter)
    period match {
      case Period(2018, None) => true
      case Period(2019, None) => true
        
      //Action Taken Date is on of before the end date of Q1 2020
      case Period(2020, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12020EndDate)
      // Action Taken Date is after Q1 2020 Ends and on/before Q2 2020 filing ends
      case Period(2020, Some("Q2")) => actionTakenDateLocal.isBetween(q12020EndDate, q22020EndDate)
      // Action Taken Date is after Q2 2020 Ends and on/before Q3 2020 filing ends
      case Period(2020, Some("Q3")) => actionTakenDateLocal.isBetween(q22020EndDate, q32020EndDate)
      case Period(2020, None) => true

      //Action Taken Date is on of before the end date of Q1 2021
      case Period(2021, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12021EndDate)
      // Action Taken Date is after Q1 2021 Ends and on/before Q2 2021 filing ends
      case Period(2021, Some("Q2")) => actionTakenDateLocal.isBetween(q12021EndDate, q22021EndDate)
      // Action Taken Date is after Q2 2021 Ends and on/before Q3 2021 filing ends
      case Period(2021, Some("Q3")) => actionTakenDateLocal.isBetween(q22021EndDate, q32021EndDate)
      case Period(2021, None) => true

      //Action Taken Date is on of before the end date of Q1 2022
      case Period(2022, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12022EndDate)
      // Action Taken Date is after Q1 2022 Ends and on/before Q2 2022 filing ends
      case Period(2022, Some("Q2")) => actionTakenDateLocal.isBetween(q12022EndDate, q22022EndDate)
      // Action Taken Date is after Q2 2022 Ends and on/before Q3 2022 filing ends
      case Period(2022, Some("Q3")) => actionTakenDateLocal.isBetween(q22022EndDate, q32022EndDate)
      case Period(2022, None) => true




    }
  }

    def actionTakenGreaterThanRange(actionTakenDate: Int, period: Period):Boolean={
      val actionTakenDateLocal = LocalDate.parse(actionTakenDate.toString,formatter)
      period match {
        case Period(2018, None) => false
        case Period(2019, None) => false

        case Period(2020, Some("Q1")) => actionTakenDateLocal.isAfter(q12020EndDate)
        case Period(2020, Some("Q2")) => actionTakenDateLocal.isAfter(q22020EndDate)
        case Period(2020, Some("Q3")) => actionTakenDateLocal.isAfter(q32020EndDate)
        case Period(2020, None) => false

        case Period(2021, Some("Q1")) => actionTakenDateLocal.isAfter(q12021EndDate)
        case Period(2021, Some("Q2")) =>  actionTakenDateLocal.isAfter(q22021EndDate)
        case Period(2021, Some("Q3")) =>  actionTakenDateLocal.isAfter(q32021EndDate)
        case Period(2021, None) => false

        case Period(2022, Some("Q1")) => actionTakenDateLocal.isAfter(q12022EndDate)
        case Period(2022, Some("Q2")) =>  actionTakenDateLocal.isAfter(q22022EndDate)
        case Period(2022, Some("Q3")) => actionTakenDateLocal.isAfter(q32022EndDate)
        case Period(2022, None) => false

      }
    }
}