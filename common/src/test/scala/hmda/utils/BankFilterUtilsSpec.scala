package hmda.util

import org.scalatest.{MustMatchers, PropSpec}
import hmda.util.BankFilterUtils._

class FilingDetailsProtobufConverterSpec extends PropSpec with MustMatchers {

  property("Must filter test instituions") {
    val institutions = List("LEI0", "B90YWS6AFX2LGWOXJ1LD")
    institutions.filter(institution =>
      filterBankWithLogging(institution)) mustBe List("LEI0")
  }

}
