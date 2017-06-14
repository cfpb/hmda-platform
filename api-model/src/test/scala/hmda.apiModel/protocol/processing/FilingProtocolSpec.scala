package hmda.apiModel.protocol.processing

import hmda.apiModel.model.{ FilingDetail, ModelGenerators }
import hmda.model.fi.Filing
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import spray.json._
import hmda.model.institution.FilingGenerators._

class FilingProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with FilingProtocol {

  property("Filing must convert to and from json") {
    forAll(filingGen) { f =>
      f.toJson.convertTo[Filing] mustBe f
    }
  }

  property("Filing detail must convert to and from json") {
    forAll(filingDetailGen) { f =>
      f.toJson.convertTo[FilingDetail] mustBe f
    }
  }

  property("A Filing must convert to JSON wrapper") {
    forAll(filingGen) { f =>
      f.toJson mustBe
        JsObject(
          ("institutionId", JsString(f.institutionId)),
          ("filingRequired", JsBoolean(f.filingRequired)),
          ("period", JsString(f.period)),
          ("status", JsObject(
            ("code", JsNumber(f.status.code)),
            ("message", JsString(f.status.message))
          )),
          ("start", JsNumber(f.start)),
          ("end", JsNumber(f.end))
        )
    }
  }

}
