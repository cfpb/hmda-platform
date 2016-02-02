package hmda.parser.fi

import hmda.parser.test.FITestData
import org.scalatest._

import scala.concurrent.ExecutionContext

class TransmittalSheetDATReaderSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  import FITestData._

  "Transmittal Sheet code" should "equals 1" in {
    val ts = TransmittalSheetDATReader(tsDAT)
    ts.id mustBe 1
  }
}
