package hmda.parser.fi

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.util.FITestData._
import org.scalatest.{ FlatSpec, MustMatchers }

class FIDataDatParserSpec extends FlatSpec with MustMatchers {

  val parser = new FIDataDatParser
  val fiData = parser.read(fiDAT)

  "FIDataDatParser" should "parse transmittal sheet" in {
    fiData.ts mustBe a[TransmittalSheet]
  }

  "FIDataDatParser" should "parse lars" in {
    fiData.lars.map(x => x mustBe a[LoanApplicationRegister])
  }

  it should "parse correct number of lars" in {
    fiData.lars.size mustBe 3
  }
}
