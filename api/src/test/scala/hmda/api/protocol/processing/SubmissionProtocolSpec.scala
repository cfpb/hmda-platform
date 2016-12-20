package hmda.api.protocol.processing

import hmda.api.model.{ MacroEditJustificationWithName, ModelGenerators }
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
          )),
          ("start", JsNumber(s.start)),
          ("end", JsNumber(s.end))
        )
    }
  }

  property("Macro edit justfication must convert to and from json") {
    forAll(macroEditJustificationWithNameGen) { m =>
      m.toJson.convertTo[MacroEditJustificationWithName] mustBe m
    }
  }

  property("Macro edit justification JSON must be in correct format") {
    forAll(macroEditJustificationWithNameGen) { m =>
      m.justification.text match {
        case None =>
          m.toJson mustBe
            JsObject(
              ("edit", JsString(m.edit)),
              ("justification", JsObject(
                ("id", JsNumber(m.justification.id)),
                ("value", JsString(m.justification.value)),
                ("verified", JsBoolean(m.justification.verified))
              ))
            )
        case Some(t) =>
          m.toJson mustBe
            JsObject(
              ("edit", JsString(m.edit)),
              ("justification", JsObject(
                ("id", JsNumber(m.justification.id)),
                ("value", JsString(m.justification.value)),
                ("verified", JsBoolean(m.justification.verified)),
                ("text", JsString(t))
              ))
            )
      }
    }
  }

}
