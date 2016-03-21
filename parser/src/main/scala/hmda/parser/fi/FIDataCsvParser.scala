package hmda.parser.fi

import hmda.model.fi.FIData
import hmda.parser.fi.lar.LarCsvParser
import hmda.parser.fi.ts.TsCsvParser

class FIDataCsvParser extends FIDataParser[String] {

  override def read(input: Iterable[String]): FIData = {
    parseLines(input)
  }

  def parseLines(lines: Iterable[String]): FIData = {
    val tsLine = lines.head
    val ts = TsCsvParser(tsLine)
    val lars = lines.tail.map(l => LarCsvParser(l))
    FIData(ts, lars)
  }

  override def readAll(input: String): FIData = {
    val lines = input.split("\n").toIterable
    parseLines(lines)
  }
}
