package hmda.messages

import hmda.messages.HmdaMessageFilter.StandardMsgKey
import org.scalatest.FunSuite

class HmdaMessageFilterTest extends FunSuite {

  test("basic") {
    testParse("LEI:LEI-2020", Some(StandardMsgKey("LasdfEI", 2020, None)))
    testParse("LEI:LEI-2020-q1", Some(StandardMsgKey("LEI", 2020, Some("q1"))))
  }

  private def testParse(key: String, expectedResult: Option[StandardMsgKey]) = {
    assert(HmdaMessageFilter.parse(key) == expectedResult)
  }

}