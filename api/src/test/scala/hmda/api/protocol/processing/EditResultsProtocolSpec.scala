package hmda.api.protocol.processing

import hmda.api.model._
import hmda.model.fi.ValidatedWithErrors
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class EditResultsProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with EditResultsProtocol {

  property("EditCollection must convert to and from json") {
    forAll(editCollectionGen) { e =>
      e.toJson.convertTo[EditCollection] mustBe e
    }
  }

  property("VerifiableEditCollection must convert to and from json") {
    forAll(verifiableEditCollectionGen) { v =>
      v.toJson.convertTo[VerifiableEditCollection] mustBe v
    }
  }

  property("Summary edit results must convert to and from json") {
    forAll(summaryEditResultsGen) { s =>
      s.toJson.convertTo[SummaryEditResults] mustBe s
    }
  }

  property("QualityEditsVerifiedResponse must have correct json format") {
    val response = EditsVerifiedResponse(true, ValidatedWithErrors)
    response.toJson mustBe JsObject(
      ("verified", JsBoolean(true)),
      ("status", JsObject(
        ("code", JsNumber(8)),
        ("message", JsString("validated with errors")),
        ("description", JsString("The data validation process is complete, but there are edits that need to be addressed. The filing process may not proceed until the file has been corrected and reuploaded."))
      ))
    )
  }

  val rowDetail1 = EditResultRow(RowId("one"), JsObject(("thing", JsString("two"))))
  property("EditResultRow must have correct JSON format") {
    rowDetail1.toJson mustBe JsObject(
      ("row", JsObject(
        ("rowId", JsString("one"))
      )),
      ("fields", JsObject(
        ("thing", JsString("two"))
      ))
    )
  }

  val errResult = EditResult("V567", Seq(rowDetail1), "uri/path/", 4, 315)

  property("Paginated EditResult must have correct JSON format") {
    errResult.toJson mustBe JsObject(
      ("edit", JsString("V567")),
      ("rows", JsArray(rowDetail1.toJson)),
      ("count", JsNumber(20)),
      ("total", JsNumber(315)),
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
