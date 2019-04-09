package hmda.util

import org.scalatest.{MustMatchers, PropSpec}
import hmda.util.BankFilterUtils._

class FilingDetailsProtobufConverterSpec extends PropSpec with MustMatchers {

  val bankFilterList = Array("LEI1", "LEI2")
  property("Must filter test instituions") {
    val institutions = List("LEI0", "LEI1", "lei2", "LEI3")
    institutions.filter(institution =>
      filterBankWithLogging(institution, bankFilterList)) mustBe List("LEI0",
                                                                      "LEI3")
  }

}
