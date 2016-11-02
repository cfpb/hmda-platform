package hmda.parser.fi

import hmda.model.ResourceUtils
import hmda.model.fi.FIData
import org.scalatest.{ FlatSpec, MustMatchers }

class FIDataCsvFileParserSpec extends FlatSpec with MustMatchers with ResourceUtils {

  "A CSV submission file" must "parse correctly" in {
    val lines = resourceLines("/txt/FirstTestBankData_clean_407_2017.txt")
    val parser = new FIDataCsvParser
    val fiData: FIData = parser.read(lines.toIterable)

    fiData.ts.id mustBe 1
    fiData.ts.respondent.name mustBe "FIRST TEST BANK"

    fiData.lars.size mustBe 412
    fiData.lars.head.loan.id mustBe "8299422144"
  }

}
