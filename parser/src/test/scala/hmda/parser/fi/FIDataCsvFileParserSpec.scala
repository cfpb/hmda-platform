package hmda.parser.fi

import hmda.model.fi.FIData
import org.scalatest.{ FlatSpec, MustMatchers }
import scala.io.Source

class FIDataCsvFileParserSpec extends FlatSpec with MustMatchers {

  "A CSV submission file" must "parse correctly" in {
    val filePath = "parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt"
    val file = Source.fromFile(filePath)
    val lines = file.getLines().toIterable

    val parser = new FIDataCsvParser
    val fiData: FIData = parser.read(lines)

    fiData.ts.id mustBe 1
    fiData.ts.respondent.name mustBe "FIRST TEST BANK"

    fiData.lars.size mustBe 412
    fiData.lars.head.loan.id mustBe "8299422144"
  }

}
