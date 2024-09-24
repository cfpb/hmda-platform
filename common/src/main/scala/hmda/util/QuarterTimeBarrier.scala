package hmda.util

import java.time.LocalDate
import hmda.util.BankFilterUtils.config
import hmda.utils.YearUtils.Period

import java.time.format.DateTimeFormatter

object QuarterTimeBarrier {
  private  val rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)

  // Builtin time format of BASIC_ISO_DATE yyyyMMdd i.e. '20111203'.
  private val formatter: DateTimeFormatter =DateTimeFormatter.BASIC_ISO_DATE
  private val  q12020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q1.actionTakenEnd)
  private val  q22020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q2.actionTakenEnd)
  private val  q32020EndDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q3.actionTakenEnd)

  private val  q12021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q1.actionTakenEnd)
  private val  q22021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q2.actionTakenEnd)
  private val  q32021EndDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q3.actionTakenEnd)

  private val  q12022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q1.actionTakenEnd)
  private val  q22022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q2.actionTakenEnd)
  private val  q32022EndDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q3.actionTakenEnd)

  private val q12023EndDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q1.actionTakenEnd)
  private val q22023EndDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q2.actionTakenEnd)
  private val q32023EndDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q3.actionTakenEnd)

  private val q12024EndDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q1.actionTakenEnd)
  private val q22024EndDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q2.actionTakenEnd)
  private val q32024EndDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q3.actionTakenEnd)

  private val q12025EndDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q1.actionTakenEnd)
  private val q22025EndDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q2.actionTakenEnd)
  private val q32025EndDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q3.actionTakenEnd)

  private val  q12020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q1.actionTakenStart)
  private val  q22020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q2.actionTakenStart)
  private val  q32020StartDate = LocalDate.ofYearDay(2020,rulesConfig.qf.q3.actionTakenStart)

  private val  q12021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q1.actionTakenStart)
  private val  q22021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q2.actionTakenStart)
  private val  q32021StartDate = LocalDate.ofYearDay(2021,rulesConfig.qf.q3.actionTakenStart)

  private val  q12022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q1.actionTakenStart)
  private val  q22022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q2.actionTakenStart)
  private val  q32022StartDate = LocalDate.ofYearDay(2022,rulesConfig.qf.q3.actionTakenStart)

  private val q12023StartDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q1.actionTakenStart)
  private val q22023StartDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q2.actionTakenStart)
  private val q32023StartDate = LocalDate.ofYearDay(2023, rulesConfig.qf.q3.actionTakenStart)

  private val q12024StartDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q1.actionTakenStart)
  private val q22024StartDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q2.actionTakenStart)
  private val q32024StartDate = LocalDate.ofYearDay(2024, rulesConfig.qf.q3.actionTakenStart)

  private val q12025StartDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q1.actionTakenStart)
  private val q22025StartDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q2.actionTakenStart)
  private val q32025StartDate = LocalDate.ofYearDay(2025, rulesConfig.qf.q3.actionTakenStart)

  implicit private class ExtendedLocalDate(date: LocalDate) {
    def isOnOrBefore(compareDate: LocalDate): Boolean = date.isBefore(compareDate) || date.isEqual(compareDate)
    def isOnOrAfter(compareDate: LocalDate): Boolean = date.isAfter(compareDate) || date.isEqual(compareDate)
    def isBetween(start: LocalDate, end: LocalDate): Boolean = date.isOnOrAfter(start) && date.isOnOrBefore(end)
  }
  
  def actionTakenInQuarterRange(actionTakenDate: Int, period: Period):Boolean= {
    val actionTakenDateLocal = LocalDate.parse(actionTakenDate.toString, formatter)
    period match {
      case Period(2018, None) => true
      case Period(2019, None) => true
        
      //Action Taken Date is on of before the end date of Q1 2020
      case Period(2020, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12020EndDate)
      // Action Taken Date is after Q1 2020 Ends and on/before Q2 2020 filing ends
      case Period(2020, Some("Q2")) => actionTakenDateLocal.isBetween(q22020StartDate, q22020EndDate)
      // Action Taken Date is after Q2 2020 Ends and on/before Q3 2020 filing ends
      case Period(2020, Some("Q3")) => actionTakenDateLocal.isBetween(q32020StartDate, q32020EndDate)
      case Period(2020, None) => true

      //Action Taken Date is on of before the end date of Q1 2021
      case Period(2021, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12021EndDate)
      // Action Taken Date is after Q1 2021 Ends and on/before Q2 2021 filing ends
      case Period(2021, Some("Q2")) => actionTakenDateLocal.isBetween(q22021StartDate, q22021EndDate)
      // Action Taken Date is after Q2 2021 Ends and on/before Q3 2021 filing ends
      case Period(2021, Some("Q3")) => actionTakenDateLocal.isBetween(q32021StartDate, q32021EndDate)
      case Period(2021, None) => true

      //Action Taken Date is on of before the end date of Q1 2022
      case Period(2022, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12022EndDate)
      // Action Taken Date is after Q1 2022 Ends and on/before Q2 2022 filing ends
      case Period(2022, Some("Q2")) => actionTakenDateLocal.isBetween(q22022StartDate, q22022EndDate)
      // Action Taken Date is after Q2 2022 Ends and on/before Q3 2022 filing ends
      case Period(2022, Some("Q3")) => actionTakenDateLocal.isBetween(q32022StartDate, q32022EndDate)
      case Period(2022, None) => true

      //Action Taken Date is on of before the end date of Q1 2023
      case Period(2023, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12023EndDate)
      // Action Taken Date is after Q1 2022 Ends and on/before Q2 2023 filing ends
      case Period(2023, Some("Q2")) => actionTakenDateLocal.isBetween(q22023StartDate, q22023EndDate)
      // Action Taken Date is after Q2 2022 Ends and on/before Q3 2023 filing ends
      case Period(2023, Some("Q3")) => actionTakenDateLocal.isBetween(q32023StartDate, q32023EndDate)
      case Period(2023, None) => true

      //Action Taken Date is on or before the end date of Q1 2024
      case Period(2024, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12024EndDate)
      // Action Taken Date is after Q1 2022 Ends and on/before Q2 2023 filing ends
      case Period(2024, Some("Q2")) => actionTakenDateLocal.isBetween(q22024StartDate, q22024EndDate)
      // Action Taken Date is after Q2 2022 Ends and on/before Q3 2023 filing ends
      case Period(2024, Some("Q3")) => actionTakenDateLocal.isBetween(q32024StartDate, q32024EndDate)
      case Period(2024, None) => true

      //Action Taken Date is on or before the end date of Q1 2023
      case Period(2025, Some("Q1")) => actionTakenDateLocal.isOnOrBefore(q12025EndDate)
      // Action Taken Date is after Q1 2022 Ends and on/before Q2 2023 filing ends
      case Period(2025, Some("Q2")) => actionTakenDateLocal.isBetween(q22025StartDate, q22025EndDate)
      // Action Taken Date is after Q2 2022 Ends and on/before Q3 2023 filing ends
      case Period(2025, Some("Q3")) => actionTakenDateLocal.isBetween(q32025StartDate, q32025EndDate)
      case Period(2025, None) => true

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

        case Period(2023, Some("Q1")) => actionTakenDateLocal.isAfter(q12023EndDate)
        case Period(2023, Some("Q2")) => actionTakenDateLocal.isAfter(q22023EndDate)
        case Period(2023, Some("Q3")) => actionTakenDateLocal.isAfter(q32023EndDate)
        case Period(2023, None) => false

        case Period(2024, Some("Q1")) => actionTakenDateLocal.isAfter(q12024EndDate)
        case Period(2024, Some("Q2")) => actionTakenDateLocal.isAfter(q22024EndDate)
        case Period(2024, Some("Q3")) => actionTakenDateLocal.isAfter(q32024EndDate)
        case Period(2024, None) => false

        case Period(2025, Some("Q1")) => actionTakenDateLocal.isAfter(q12025EndDate)
        case Period(2025, Some("Q2")) => actionTakenDateLocal.isAfter(q22025EndDate)
        case Period(2025, Some("Q3")) => actionTakenDateLocal.isAfter(q32025EndDate)
        case Period(2025, None) => false

      }
    }
}