package hmda.parser.fi.ts

import hmda.parser.util.FITestData
import org.scalatest._

import scala.concurrent.ExecutionContext

class TsDatParserSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  import FITestData._

  "Transmittal Sheet code" should "equals 1" in {
    val ts = TsDatParser(tsDAT)
    ts.id mustBe 1
  }
}
