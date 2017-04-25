package hmda.api.protocol.processing

import hmda.api.model.{ ModelGenerators, ParsingErrorSummary }
import hmda.model.fi.ParsedWithErrors
import hmda.parser.fi.lar.LarParsingError
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class ParserResultsProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with ParserResultsProtocol {

  val tsErrors = List("ts err 1", "ts err 2")
  val larError = LarParsingError(3, List("first", "second"))
  val larError2 = LarParsingError(9, List("third", "fourth"))
  val parsingErrorSummary = ParsingErrorSummary(tsErrors, Seq(larError, larError2), "uri/path/", 4, 315, ParsedWithErrors)

  val expectedLarErrorJson = JsObject(
    ("lineNumber", JsNumber(3)),
    ("errorMessages", JsArray(JsString("first"), JsString("second")))
  )
  val expectedLarError2Json = JsObject(
    ("lineNumber", JsNumber(9)),
    ("errorMessages", JsArray(JsString("third"), JsString("fourth")))
  )

  property("A LAR Parsing Error must have correct JSON format") {
    larError.toJson mustBe expectedLarErrorJson
  }

  property("Paginated Parsing Error Summary must have correct JSON format") {
    parsingErrorSummary.toJson mustBe JsObject(
      ("transmittalSheetErrors", JsArray(JsString("ts err 1"), JsString("ts err 2"))),
      ("larErrors", JsArray(expectedLarErrorJson, expectedLarError2Json)),
      ("count", JsNumber(20)),
      ("total", JsNumber(315)),
      ("status", JsObject(
        ("code", JsNumber(5)),
        ("message", JsString("parsed with errors")),
        ("description", JsString("The data are not formatted according to certain formatting requirements specified in the Filing Instructions Guide. The filing process may not proceed until the data have been corrected and the file has been reuploaded."))
      )),
      ("_links", JsObject(
        ("href", JsString("uri/path/{rel}")),
        ("self", JsString("?page=4")),
        ("first", JsString("?page=1")),
        ("prev", JsString("?page=3")),
        ("next", JsString("?page=5")),
        ("last", JsString("?page=16"))
      ))
    )

  }

}
