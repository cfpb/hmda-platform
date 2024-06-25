package hmda.messages

import hmda.messages.HmdaMessageFilter.StandardMsg
import org.scalatest.FunSuite

class HmdaMessageFilterTest extends FunSuite {

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "B90YWS6AFX2LGWOXJ1LD-2020",
    Some(StandardMsg("B90YWS6AFX2LGWOXJ1LD", 2020, None, None))
  )

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "B90YWS6AFX2LGWOXJ1LD-2020-q1",
    Some(StandardMsg("B90YWS6AFX2LGWOXJ1LD", 2020, Some("q1"), None))
  )

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "B90YWS6AFX2LGWOXJ1LD-2020-3",
    Some(StandardMsg("B90YWS6AFX2LGWOXJ1LD", 2020, None, Some("3")))
  )

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "B90YWS6AFX2LGWOXJ1LD-2020-Q3-343",
    Some(StandardMsg("B90YWS6AFX2LGWOXJ1LD", 2020, Some("Q3"), Some("343")))
  )

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "2020",
    None
  )

  testParse(
    "B90YWS6AFX2LGWOXJ1LD",
    "Q3",
    None
  )

  private def testParse(key: String, value: String, expectedResult: Option[StandardMsg]) =
    test(s"$key:$value") {
      assert(HmdaMessageFilter.parse(key, value) == expectedResult)
    }

}