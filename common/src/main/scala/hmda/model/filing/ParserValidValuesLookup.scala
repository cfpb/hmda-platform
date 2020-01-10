package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._

object ParserValidValuesLookup {

  val config = ConfigFactory.load()
  val parserValidValuesFile =
    config.getString("hmda.filing.parser.validValues.filename")

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

  def lookupParserValidValues(fieldName: String): String =
    parserValidValuesMap
      .getOrElse(fieldName, "")

}
