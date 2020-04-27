package hmda.util

import org.scalatest.{ MustMatchers, PropSpec }
import hmda.util.BankFilterUtils._

class FilingDetailsProtobufConverterSpec extends PropSpec with MustMatchers {

  property("Must filter test institutions") {
    val institutions = List("LEI0", "B90YWS6AFX2LGWOXJ1LD")
    institutions.filter(filterBankWithLogging) mustBe List("LEI0")
  }

}