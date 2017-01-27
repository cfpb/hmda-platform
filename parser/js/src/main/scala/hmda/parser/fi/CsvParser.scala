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
    if (parsed.isRight) {
      parsed.right.get
    } else {
      parsed.left.get
    }
  }

  def parseLar(lar: String) = {
    val parsed = LarCsvParser(lar)
    if (parsed.isRight) {
      parsed.right.get
    } else {
      parsed.left.get
    }
  }

}
