package hmda.parser.fi

import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSApp

@JSExportAll
object CsvParser extends JSApp {

  def main(): Unit = {

  }

  def parseTs(ts: String) = {
    val parsed = TsCsvParser(ts)
    parsed match {
      case Right(x) => x
      case Left(x) => x
    }
  }

  def parseLar(lar: String) = {
    val parsed = LarCsvParser(lar)
    parsed match {
      case Right(x) => x
      case Left(x) => x
    }
  }

}
