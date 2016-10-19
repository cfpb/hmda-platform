package hmda.api.protocol.processing

import hmda.api.model.ModelGenerators
import hmda.model.fi.Submission
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class SubmissionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with SubmissionProtocol {

  property("Submission must convert to and from json") {
    forAll(submissionGen) { s =>
      s.toJson.convertTo[Submission] mustBe s
    }
  }

  property("Submission JSON must be correct format") {
    forAll(submissionGen) { s =>
      s.toJson mustBe
        JsObject(
          ("id", JsObject(
            ("institutionId", JsString(s.id.institutionId)),
            ("period", JsString(s.id.period)),
            ("sequenceNumber", JsNumber(s.id.sequenceNumber))
          )),
          ("status", JsObject(
            ("code", JsNumber(s.status.code)),
            ("message", JsString(s.status.message))
          ))
        )
    }
  }

}
