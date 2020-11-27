package hmda.messages

import hmda.messages.HmdaMessageFilter.StandardMsgKey
import org.scalatest.FunSuite

class HmdaMessageFilterTest extends FunSuite {

  testParse("LEI:LEI-2020", Some(StandardMsgKey("LEI", 2020, None, None)))
  testParse("LEI:LEI-2020-q1", Some(StandardMsgKey("LEI", 2020, Some("q1"), None)))
  testParse("B90YWS6AFX2LGWOXJ1LD:B90YWS6AFX2LGWOXJ1LD-2020-3", Some(StandardMsgKey("B90YWS6AFX2LGWOXJ1LD", 2020, None, Some("3"))))
  testParse("B90YWS6AFX2LGWOXJ1LD:B90YWS6AFX2LGWOXJ1LD-2020-Q3-343", Some(StandardMsgKey("B90YWS6AFX2LGWOXJ1LD", 2020, Some("Q3"), Some("343"))))
  testParse("B90YWS6AFX2LGWOXJ1LD:2020", None)
  testParse("B90YWS6AFX2LGWOXJ1LD:Q3", None)



  private def testParse(key: String, expectedResult: Option[StandardMsgKey]) = {
    test(key) {
      assert(HmdaMessageFilter.parse(key) == expectedResult)
    }
  }

}