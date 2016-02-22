package hmda.parser.fi

import hmda.model.fi.FIData
import hmda.parser.fi.lar.LarDatParser
import hmda.parser.fi.ts.TsDatParser
import scala.scalajs.js.annotation.JSExport

@JSExport
class FIDataDatParser extends FIDataParser[String] {

  override def read(input: Iterable[String]): FIData = {
    parseLines(input)
  }

  @JSExport
  override def readAll(input: String): FIData = {
    val lines = input.split("\n").toIterable
    parseLines(lines)
  }

  def parseLines(lines: Iterable[String]): FIData = {
    val tsLine = lines.head
    val ts = TsDatParser(tsLine)
    val lars = lines.tail.map(l => LarDatParser(l))
    FIData(ts, lars)
  }
}
