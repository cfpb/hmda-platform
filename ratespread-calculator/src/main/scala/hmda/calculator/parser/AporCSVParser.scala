package hmda.calculator.parser

import java.time.{LocalDate, Month}

import hmda.calculator.apor.APOR

import scala.util.Try

object APORCsvParser {

  def apply(s: String): APOR = {
    val values = s.split('|').map(_.trim)
    val dateString = values.head
    val dateComponents = dateString.split('/').map(_.trim)
    val month = Try(Month.of(dateComponents(0).toInt)).getOrElse(Month.of(1))
    val day = Try(dateComponents(1).toInt).getOrElse(1)
    val year = Try(dateComponents(2).toInt).getOrElse(2000)
    val date = LocalDate.of(year, month, day)
    val aporList = values.tail.map(_.toDouble)
    APOR(date, aporList)
  }
}
