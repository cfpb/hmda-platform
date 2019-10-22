package hmda.utils

import com.typesafe.config.ConfigFactory

object YearUtils {

  val config = ConfigFactory.load()

  val firstYear   = config.getString("hmda.filing.first_year")
  val currentYear = config.getString("hmda.filing.current")

  def isValidYear(year: Int): Boolean =
    (year >= firstYear.toInt) && (year <= currentYear.toInt)

  def isValidQuarter(quarter: String): Boolean =
    quarter.length == 2 &&
      quarter.head == 'Q' &&
      quarter.last.isDigit && {
      val digit = quarter.last.toString.toInt
      digit >= 1 && digit <= 3
    }

  def period(year: Int, quarter: Option[String]): String =
    quarter.fold(ifEmpty = s"$year")(quarter => s"$year-$quarter")
}
