package hmda.parser.fi

import org.scalatest._

import scala.concurrent.ExecutionContext

class TransmittalSheetDATReaderSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val tsDAT = "101234567899201301171330 201399-99999990000900MIKES SMALL BANK   XXXXXXXXXXX1234 Main St       XXXXXXXXXXXXXXXXXXXXXSacramento         XXXXXXCA99999-9999MIKES SMALL INC    XXXXXXXXXXX1234 Kearney St    XXXXXXXXXXXXXXXXXXXXXSan Francisco      XXXXXXCA99999-1234Mrs. Krabappel     XXXXXXXXXXX916-999-9999999-753-9999krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

  "Transmittal Sheet code" should "equals 1" in {
    val ts = TransmittalSheetDATReader(tsDAT)
    ts.id mustBe 1
  }
}
