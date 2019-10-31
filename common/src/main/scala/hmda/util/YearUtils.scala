package hmda.utils

import com.typesafe.config.ConfigFactory
import cats.implicits._
import hmda.model.filing.submission.SubmissionId

object YearUtils {

  val config = ConfigFactory.load()

  val firstYear   = config.getString("hmda.filing.first_year")
  val currentYear = config.getString("hmda.filing.current")
  val quarterYear = config.getString("hmda.filing.quarter_year")

  def isValidYear(year: Int): Boolean =
    (year >= firstYear.toInt) && (year <= quarterYear.toInt)

  def isValidQuarter(quarter: String): Boolean =
    quarter match {
      case "Q1" => true
      case "Q2" => true
      case "Q3" => true
      case _    => false
    }

  def period(year: Int, quarter: Option[String]): String =
    quarter.fold(ifEmpty = s"$year")(quarter => s"$year-$quarter")

  case class Period(year: Int, quarter: Option[String])

  def parsePeriod(period: String): Either[Exception, Period] = {
    val raw = period.split("-")
    if (raw.length == 1) Period(raw(0).toInt, None).asRight
    else if (raw.length == 2) Period(raw(0).toInt, Option(raw(1))).asRight
    else new Exception(s"Failed to parse invalid period: $period").asLeft
  }

  def isQuarterlyFiling(submissionId: SubmissionId): Boolean = {
    val period = parsePeriod(submissionId.period).right.get
    period.quarter match {
      case None =>
        false
      case _ =>
        true
    }
  }
}
