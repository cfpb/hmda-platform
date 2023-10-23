package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
 import hmda.utils.YearUtils.Period

object ParserValidValuesLookup {

  val config = ConfigFactory.load()
  val parserValidValuesFile =
    config.getString("hmda.filing.parser.validValues.filename")

  val parserValidValuesFile2024 =
    config.getString("hmda.filing.parser.validValues.filename2024")

  def parserValidValuesMapCreator(
      file: Iterable[String]): Map[String, String] = {
    file
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        val fieldName = values(0)
        val validValues = values(1)
        (fieldName, validValues)
      }
      .toMap
  }

  val parserValidValuesLines = fileLines(s"/$parserValidValuesFile")
  val parserValidValuesMap = parserValidValuesMapCreator(parserValidValuesLines)

  val parserValidValuesLines2024 = fileLines(s"/$parserValidValuesFile2024")
  val parserValidValuesMap2024 = parserValidValuesMapCreator(parserValidValuesLines2024)

  def lookupParserValidValues(fieldName: String): String = {

    parserValidValuesMap
      .getOrElse(fieldName, "")
  }


  def lookupParserValidValuesByYear(fieldName: String,period: Period): String =
    period match {
      case Period(yr, _) if yr < 2024 => {parserValidValuesMap.getOrElse(fieldName, "")}
      case _ => {parserValidValuesMap2024.getOrElse(fieldName, "")}
    }
}
