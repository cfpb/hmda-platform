package hmda.util
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import cats.implicits._

import java.time.temporal.{ ChronoField, TemporalAccessor }
import com.typesafe.config.{ Config, ConfigFactory }
import org.slf4j.LoggerFactory

import scala.util.Try

object Filer {
  private val log = LoggerFactory.getLogger(getClass)

  private val dateFormatter = new DateTimeFormatterBuilder().appendPattern("MMMM dd yyyy").toFormatter

  private val rtTgConfig = {
    val tgWatch = ConfigFactory.load().getConfig("hmda.cm_watch.timed_guards")
    val ns = Try(tgWatch.getString("ns")).getOrElse("default")
    val cmName = Try(tgWatch.getString("name")).getOrElse("timed-guards")
    new RealTimeConfig(cmName, ns)
  }

  def check(filingRulesConfig: FilingRulesConfig)(year: Int, dayOfYear: Int, quarter: Option[String]): Boolean = {
    val rulesConfig = Try(getRulesFromRtConfig()).getOrElse(filingRulesConfig)

    import rulesConfig._
    import qf._

    def resolveQuarter(q: String): QuarterConfig = q match {
      case "Q1" => q1
      case "Q2" => q2
      case _    => q3
    }

    quarter match {
      case None =>
        filingYearsAllowed.contains(year)

      case Some(q) =>
        quarterlyFilingYearsAllowed.contains(year) && checkQuarter(dayOfYear, resolveQuarter(q))
    }
  }

  def checkQuarterlyYear(filingRulesConfig: FilingRulesConfig)(year: Int): Boolean = {
    import filingRulesConfig._
    import qf._
    filingYearsAllowed.contains(year) || quarterlyFilingYearsAllowed.contains(year)
  }

  def parse(hocon: Config, actualYear: Int = 0): Either[String, FilingRulesConfig] = {
    // note that we expect the user to fill in year the month and date, and we fill in the year
    val formatter = new DateTimeFormatterBuilder().appendPattern("MMMM dd yyyy").toFormatter
    val year: String = if (actualYear != 0) {
      " " + actualYear
    } else {
      " " + LocalDate.now().getYear
    }

    def parseQuarterConfig(hocon: Config): Either[String, QuarterConfig] = {
      for {
        rawStart <- Try(hocon.getString("start")).toEither.left.map(_ => "failed to obtain start")
        rawEnd   <- Try(hocon.getString("end")).toEither.left.map(_ => "failed to obtain end")
        start    <- Try(formatter.parse(rawStart + year)).toEither.left.map(_ => s"failed to parse $rawStart as a valid start date")
        end      <- Try(formatter.parse(rawEnd + year)).toEither.left.map(_ => s"failed to parse $rawEnd as a valid end date")
        actionTakenStart <- Try(getDateConfigWithDefault(hocon, "action_date_start", start)).toEither.left.map(_ => "Invalid action taken start date")
        actionTakenEnd <- Try(getDateConfigWithDefault(hocon , "action_date_end", end)).toEither.left.map(_ => "Invalid action taken end date")
        c <- Try(
          QuarterConfig(
            start.get(ChronoField.DAY_OF_YEAR),
            end.get(ChronoField.DAY_OF_YEAR),
            actionTakenStart.get(ChronoField.DAY_OF_YEAR),
            actionTakenEnd.get(ChronoField.DAY_OF_YEAR)
          )
        ).toEither.left.map(e => s"failed to build config because dates weren't valid ${e.getMessage}")
      } yield c
    }

    def getDateConfigWithDefault(hocon: Config, key: String, defaultDate: TemporalAccessor): TemporalAccessor = {
      val configVal = Try(hocon.getString(key)).getOrElse("")
      if (configVal == "") defaultDate else formatter.parse(configVal + year)
    }

    def parseYear(s: String): Either[String, Int] =
      Try(s.toInt).toEither.left.map(e => s"failed to parse $s as a valid year because ${e.getMessage}")

    def parseYears(s: String): Either[String, List[Int]] =
      s.split(",").map(parseYear).toList.sequence.flatMap {
        case l if l.nonEmpty => Right(l)
        case _               => Left("Provide a comma separated list of years")
      }

    val rtRules = Try(getRulesFromRtConfig()).toEither.left.map(t => {
      log.warn(s"Failed to load time guard through real time config: ${t.getMessage}", t)
      "Failed real time config retrieval"
    })

    if (rtRules.isRight) {
      rtRules
    } else {
      for {
        yearlyFilingYearsAllowedC <- Try(hocon.getString("hmda.rules.yearly-filing.years-allowed")).toEither.left.map(_ =>
          "Failed to get HOCON: hmda.rules.yearly-filing.years-allowed"
        )
        quarterlyFilingYearsAllowedC <- Try(hocon.getString("hmda.rules.quarterly-filing.years-allowed")).toEither.left.map(_ =>
          "Failed to get HOCON: hmda.rules.quarterly-filing.years-allowed"
        )
        q1C <- Try(hocon.getConfig("hmda.rules.quarterly-filing.q1")).toEither.left.map(_ => "Failed to get HOCON for q1")
        q2C <- Try(hocon.getConfig("hmda.rules.quarterly-filing.q2")).toEither.left.map(_ => "Failed to get HOCON for q2")
        q3C <- Try(hocon.getConfig("hmda.rules.quarterly-filing.q3")).toEither.left.map(_ => "Failed to get HOCON for q3")
        yearsAllowedForYearlyFiling <- parseYears(yearlyFilingYearsAllowedC)
        quarterlyFilingYearsAllowed <- parseYears(quarterlyFilingYearsAllowedC)
        q1 <- parseQuarterConfig(q1C)
        q2 <- parseQuarterConfig(q2C)
        q3 <- parseQuarterConfig(q3C)
      } yield FilingRulesConfig(QuarterlyFilingConfig(quarterlyFilingYearsAllowed, q1, q2, q3), yearsAllowedForYearlyFiling)
    }
  }

  private def getRulesFromRtConfig(): FilingRulesConfig = {
    val quarterlyFilingConfig = QuarterlyFilingConfig(
      rtTgConfig.getString("quarterlyYearsAllowed").split(",").map(_.toInt).toList,
      getQuarterConfig(1),
      getQuarterConfig(2),
      getQuarterConfig(3)
    )
    FilingRulesConfig(quarterlyFilingConfig, rtTgConfig.getString("yearsAllowed").split(",").map(_.toInt).toList)
  }

  private def getQuarterConfig(quarter: Int): QuarterConfig = {
    val currentYear = LocalDate.now().getYear
    val startDate = rtTgConfig.getString(s"q${quarter}Start")
    val endDate = rtTgConfig.getString(s"q${quarter}End")
    val actionStartDate = rtTgConfig.getString(s"actionQ${quarter}Start")
    val actionEndDate = rtTgConfig.getString(s"actionQ${quarter}End")
    QuarterConfig(
      dateFormatter.parse(s"$startDate $currentYear").get(ChronoField.DAY_OF_YEAR),
      dateFormatter.parse(s"$endDate $currentYear").get(ChronoField.DAY_OF_YEAR),
      dateFormatter.parse(s"$actionStartDate $currentYear").get(ChronoField.DAY_OF_YEAR),
      dateFormatter.parse(s"$actionEndDate $currentYear").get(ChronoField.DAY_OF_YEAR),
    )
  }

  private def checkQuarter(dayOfYear: Int, quarterConfig: QuarterConfig): Boolean =
    (dayOfYear >= quarterConfig.startDayOfYear) && (dayOfYear <= quarterConfig.endDayOfYear)

  case class QuarterConfig(startDayOfYear: Int, endDayOfYear: Int, actionTakenStart: Int, actionTakenEnd: Int)
  case class QuarterlyFilingConfig(quarterlyFilingYearsAllowed: List[Int], q1: QuarterConfig, q2: QuarterConfig, q3: QuarterConfig)
  case class FilingRulesConfig(qf: QuarterlyFilingConfig, filingYearsAllowed: List[Int])
}