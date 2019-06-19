package hmda.utils

import com.typesafe.config.ConfigFactory

object YearUtils {

  val config = ConfigFactory.load()

  val firstYear = config.getString("hmda.filing.first_year")
  val currentYear = config.getString("hmda.filing.current")

  def isValidYear(year: Int): Boolean = {
    (year >= firstYear.toInt) && (year <= currentYear.toInt)
  }

}
