package hmda.util

import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

import cats.implicits._
import com.typesafe.config.Config

import scala.util.Try

object Filer {
  def check(filingRulesConfig: FilingRulesConfig)(year: Int, dayOfYear: Int, quarter: Option[String]): Boolean = {
    import filingRulesConfig._
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
        (year >= startYear) && filingYearsAllowed.contains(year) && checkQuarter(dayOfYear, resolveQuarter(q))
    }
  }

  def parse(hocon: Config): Either[String, FilingRulesConfig] = {
    def parseQuarterConfig(hocon: Config): Either[String, QuarterConfig] = {
      // note that we expect the user to fill in year the month and date, and we fill in the year
      val formatter = new DateTimeFormatterBuilder().appendPattern("MMMM dd yyyy").toFormatter
      // just a random year so we can get DAY_OF_YEAR to resolve to make quarterly range checks easier
      val year = " 2099"
      for {
        rawStart <- Try(hocon.getString("start")).toEither.left.map(_ => "failed to obtain start")
        rawEnd <- Try(hocon.getString("end")).toEither.left.map(_ => "failed to obtain end")
        enabled <- Try(hocon.getBoolean("enabled")).toEither.left.map(_ => "failed to obtain enabled")
        start <- Try(formatter.parse(rawStart + year)).toEither.left.map(_ => s"failed to parse $rawStart as a valid start date")
        end <- Try(formatter.parse(rawEnd + year)).toEither.left.map(_ => s"failed to parse $rawEnd as a valid end date")
        c <- Try(QuarterConfig(
          start.get(ChronoField.DAY_OF_YEAR),
          end.get(ChronoField.DAY_OF_YEAR),
          enabled
        )
        ).toEither.left.map(e => s"failed to build config because dates weren't valid ${e.getMessage}")
      } yield c
    }

    def parseYear(s: String): Either[String, Int] = Try(s.toInt).filter(_ > 1000).toEither.left.map(e => s"failed to parse $s as a valid year because ${e.getMessage}")

    def parseYears(s: String): Either[String, List[Int]] = {
      s.split(",").map(parseYear).toList.sequence.flatMap {
        case l if l.nonEmpty => Right(l)
        case _ => Left("Provide a comma separated list of years")
      }
    }

    for {
      filingYearsAllowedC <- Try(hocon.getString("rules.filing-years-allowed")).toEither.left.map(_ => "Failed to get HOCON: rules.filing-years-allowed")
      startYearC <- Try(hocon.getString("rules.quarterly-filing.start-year")).toEither.left.map(_ => "Failed to get HOCON: rules.quarterly-filing.start-year")
      q1C <- Try(hocon.getConfig("rules.quarterly-filing.q1")).toEither.left.map(_ => "Failed to get HOCON for q1")
      q2C <- Try(hocon.getConfig("rules.quarterly-filing.q2")).toEither.left.map(_ => "Failed to get HOCON for q2")
      q3C <- Try(hocon.getConfig("rules.quarterly-filing.q3")).toEither.left.map(_ => "Failed to get HOCON for q3")
      yearsAllowed <- parseYears(filingYearsAllowedC)
      startYear <- parseYear(startYearC)
      q1 <- parseQuarterConfig(q1C)
      q2 <- parseQuarterConfig(q2C)
      q3 <- parseQuarterConfig(q3C)
    } yield FilingRulesConfig(QuarterlyFilingConfig(startYear, q1, q2, q3), yearsAllowed)
  }

  private def checkQuarter(dayOfYear: Int, quarterConfig: QuarterConfig): Boolean =
    quarterConfig.enabled && (dayOfYear >= quarterConfig.startDayOfYear) && (dayOfYear <= quarterConfig.endDayOfYear)

  case class QuarterConfig(startDayOfYear: Int, endDayOfYear: Int, enabled: Boolean)
  case class QuarterlyFilingConfig(startYear: Int, q1: QuarterConfig, q2: QuarterConfig, q3: QuarterConfig)
  case class FilingRulesConfig(qf: QuarterlyFilingConfig, filingYearsAllowed: List[Int])
}