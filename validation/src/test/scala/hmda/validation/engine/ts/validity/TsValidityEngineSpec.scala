package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TsGenerators }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.UndeterminedExternalId
import hmda.model.institution.{ ExternalId, Institution, TopHolder }
import hmda.model.institution.InstitutionType.{ Bank, MBS }
import hmda.validation.context.ValidationContext
import hmda.validation.rules.ts.validity.ValidityUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

import scala.concurrent.ExecutionContext

class TsValidityEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsValidityEngine with ScalaFutures with ValidityUtils {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val ctx = ValidationContext(None, None)

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
      val respondent = hmda.model.institution.Respondent(ExternalId("1", UndeterminedExternalId), "test bank", "", "", "")
      val parent = hmda.model.institution.Parent("123", 123, "test parent", "", "")
      val topHolder = TopHolder(-1, "", "", "", "")
      val someMBS = Some(Institution("1", CFPB, 2017, MBS, cra = true, Set(), Set(), respondent, hmdaFilerFlag = true, parent, 0, 0, topHolder))
      val badTs = ts.copy(parent = Parent("", "", "", "", ""))
      checkValidity(badTs, ValidationContext(someMBS, None)).isFailure mustBe true
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
