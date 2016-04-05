package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.{ Contact, Respondent }
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.rules.ts.validity.ValidityUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext

class TsValidityEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsValidityEngine with ScalaFutures with ValidityUtils {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      whenever(respondentNotEmpty(ts.respondent)) {
        checkValidity(ts).isSuccess mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V105 (Respondent Mailing Address)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(respondent = Respondent("", "", "", "", "", ""))
        checkValidity(badTs).isFailure mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V140 (Respondent State Code)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(respondent = Respondent("", "", "", "", "XXX", ""))
        val badTs2 = ts.copy(respondent = Respondent("", "", "", "", "XX", ""))
        checkValidity(badTs).isFailure mustBe true
        checkValidity(badTs2).isFailure mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V155 (Respondent E-Mail Address)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(contact = Contact("", "", "", "sqwdqw"))
        val badTs2 = ts.copy(contact = Contact("", "", "", ""))
        checkValidity(badTs).isFailure mustBe true
        checkValidity(badTs2).isFailure mustBe true
      }
    }
  }

}
