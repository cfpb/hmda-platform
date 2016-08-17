package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.{ Contact, Parent, Respondent }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType.MBS
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.context.ValidationContext
import hmda.validation.rules.ts.validity.ValidityUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

import scala.concurrent.ExecutionContext

class TsValidityEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsValidityEngine with ScalaFutures with ValidityUtils {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val ctx = ValidationContext(None)

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      whenever(respondentNotEmpty(ts.respondent) && (ts.contact.name != ts.respondent.name)) {
        checkValidity(ts, ctx).isSuccess mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V105 (Respondent Mailing Address)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(respondent = Respondent("", "", "", "", "", ""))
        checkValidity(badTs, ctx).isFailure mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V110 (Parent info)") {
    forAll(tsGen) { ts =>
      val someMBS = Some(Institution(1, "Test MBS", Set(), CFPB, MBS, hasParent = true))
      val badTs = ts.copy(parent = Parent("", "", "", "", ""))
      checkValidity(badTs, ValidationContext(someMBS)).isFailure mustBe true
    }
  }

  property("Transmittal Sheet fails V140 (Respondent State Code)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(respondent = Respondent("", "", "", "", "XXX", ""))
        val badTs2 = ts.copy(respondent = Respondent("", "", "", "", "XX", ""))
        checkValidity(badTs, ctx).isFailure mustBe true
        checkValidity(badTs2, ctx).isFailure mustBe true
      }
    }
  }

  property("Transmittal Sheet fails V155 (Respondent E-Mail Address)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = ts.copy(contact = Contact("", "", "", "sqwdqw"))
        val badTs2 = ts.copy(contact = Contact("", "", "", ""))
        checkValidity(badTs, ctx).isFailure mustBe true
        checkValidity(badTs2, ctx).isFailure mustBe true
      }
    }
  }

}
