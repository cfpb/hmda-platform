package hmda.utils

import com.typesafe.config.ConfigFactory
import cats.implicits._
import hmda.model.filing.submission.SubmissionId

object YearUtils {

  val config = ConfigFactory.load()

  val currentYear = config.getString("hmda.filing.current")

  def isValidQuarter(quarter: String): Boolean =
    quarter match {
      case "Q1" => true
      case "Q2" => true
      case "Q3" => true
      case _    => false
    }

  def isValidQuarterTS(quarter: Int): Boolean =
    quarter match {
      case 1 => true
      case 2 => true
      case 3 => true
      case _ => false
    }

  def period(year: Int, quarter: Option[String]): String =
    quarter.fold(ifEmpty = s"$year")(quarter => s"$year-$quarter")

  case class Period(year: Int, quarter: Option[String]) {
    override def toString: String = {
      quarter match {
        case Some(quarter) =>
          s"$year-$quarter"
        case None =>
          year.toString
        case _ =>
          year.toString
      }
    }
  }

  def parsePeriod(period: String): Either[Exception, Period] = {
    val raw = period.split("-")
    if (raw.length == 1) Period(raw(0).toInt, None).asRight
    else if (raw.length == 2) Period(raw(0).toInt, Option(raw(1))).asRight
    else new Exception(s"Failed to parse invalid period: $period").asLeft
  }
  def isQuarterlyFiling(submissionId: SubmissionId): Boolean = {
    submissionId.period.quarter match {
      case None =>
        false
      case _ =>
        true
    }
  }
}
