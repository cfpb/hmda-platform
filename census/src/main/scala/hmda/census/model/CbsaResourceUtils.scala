package hmda.census.model

import com.github.tototoshi.csv.CSVReader
import hmda.model.ResourceUtils

trait CbsaResourceUtils extends ResourceUtils {

  def smallCountyChecker(population: Int) = {
    if (population < 30000) "1" else "0"
  }

  def leftPad(characters: Int, input: String): String = {
    ("0" * characters + input).takeRight(characters)
  }

  def csvLines(filename: String): List[List[String]] = {
    CSVReader.open(resource(filename, "ISO-8859-1")).all()
  }

}
