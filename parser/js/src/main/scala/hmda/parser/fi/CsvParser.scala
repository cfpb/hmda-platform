package hmda.parser.fi

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.lar.{ LarCsvParser, LarParsingError }
import hmda.parser.fi.ts.TsCsvParser
import scala.scalajs.js.annotation.{ JSExport, JSExportAll }
import scala.scalajs.js.JSApp

@JSExportAll
object CsvParser extends JSApp {

  def main(): Unit = {

  }

  def parseTs(ts: String): Either[List[String], TransmittalSheet] = {
    TsCsvParser(ts)
  }

  def parseLar(lar: String): Either[LarParsingError, LoanApplicationRegister] = {
    LarCsvParser(lar)
  }

}
