package hmda.parser.fi.panel

import scala.io.Source
import scala.util.Try

object PanelCsvParser extends App {
  var file: Source = _
  if (Try(Source.fromFile(args(0))).isSuccess) {
    file = Source.fromFile(args(0))
  } else {
    println("\nWARNING: Unable to read file input.")
    System.exit(1)
  }

  val lines = file.getLines().toList.tail

  for (line <- lines) {
    val i = InstitutionParser(line)
    println(i)
  }
}
