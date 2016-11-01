package model

import scala.io.{ BufferedSource, Source }

trait CbsaResourceUtils {

  def smallCountyChecker(population: Int) = {
    if (population < 3000) "1" else "0"
  }

  def resourceIso(filename: String): BufferedSource = {
    val file = getClass.getResourceAsStream(filename)
    Source.fromInputStream(file, "ISO-8859-1")
  }

  def resourceLinesIso(filename: String): Iterator[String] = {
    resourceIso(filename).getLines()
  }

}
