package model

import com.github.tototoshi.csv.CSVReader

import scala.io.{ BufferedSource, Source }

trait CbsaResourceUtils {

  def smallCountyChecker(population: Int) = {
    if (population < 30000) "1" else "0"
  }

  def leftPad(characters: Int, input: String): String = {
    ("0" * characters + input).takeRight(characters)
  }

  def resourceIso(filename: String): BufferedSource = {
    val file = getClass.getResourceAsStream(filename)
    Source.fromInputStream(file, "ISO-8859-1")
  }

  def resourceLinesIso(filename: String): Iterator[String] = {
    resourceIso(filename).getLines()
  }

  def csvLines(filename: String): List[List[String]] = {
    CSVReader.open(resourceIso(filename)).all()
  }

}
