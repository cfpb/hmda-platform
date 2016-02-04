package hmda.parser.fi

import hmda.model.fi.FIData
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser

import scala.scalajs.js.annotation.JSExport

@JSExport
class FIDataCsvParser extends FIDataParser[String] {
  @JSExport
  override def read(input: Iterable[String]): FIData = {
    val tsLine = input.head
    val ts = TsCsvParser(tsLine)
    val lars = input.tail.iterator.map(l => LarCsvParser(l))
    FIData(ts, lars)
  }
}
