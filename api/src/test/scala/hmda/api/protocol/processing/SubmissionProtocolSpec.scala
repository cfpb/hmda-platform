package hmda.api.protocol.processing

import hmda.api.model.institutions.submissions.SubmissionSummary
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
            ("message", JsString(s.status.message)),
            ("description", JsString(s.status.description))
          )),
          ("receipt", JsString(s.receipt)),
          ("start", JsNumber(s.start)),
          ("end", JsNumber(s.end))
        )
    }
  }

  property("Submission Summary must convert to and from json") {
    forAll(submissionSummaryGen) { s =>
      s.toJson.convertTo[SubmissionSummary] mustBe s
    }
  }

  property("Submission Summary must be correct format") {
    forAll(submissionSummaryGen) { s =>
      s.toJson mustBe
        JsObject(
          ("respondent", JsObject(
            ("name", JsString(s.respondent.name)),
            ("id", JsString(s.respondent.id)),
            ("taxId", JsString(s.respondent.taxId)),
            ("agency", JsString(s.respondent.agency)),
            ("contact", JsObject(
              ("name", JsString(s.respondent.contact.name)),
              ("phone", JsString(s.respondent.contact.phone)),
              ("email", JsString(s.respondent.contact.email))
            ))
          )),
          ("file", JsObject(
            ("name", JsString(s.file.name)),
            ("year", JsString(s.file.year)),
            ("totalLARS", JsNumber(s.file.totalLARS))
          ))
        )
    }
  }

}
