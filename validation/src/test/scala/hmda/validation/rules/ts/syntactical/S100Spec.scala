package hmda.validation.rules.ts.syntactical

import hmda.model.fi.lar._
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, RssdId }
import hmda.model.institution.InstitutionStatus.Active
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution.{ ExternalId, Institution }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.{ MustMatchers, WordSpec }

class S100Spec extends WordSpec with MustMatchers {

  "S100" must {
    "be named S100" in {
      val ctx = ValidationContext(None, Some(2017))

      S100.inContext(ctx).name mustBe "S100"
    }
  }

  "S100 for TS" must {

    val ts = TransmittalSheet(
      1,
      CFPB.value,
      201602021453L,
      2017,
      "12-3456789",
      10000, Respondent("999999", "Test Bank", "1234 Bank St.", "Test Bank", "CA", "99999"),
      Parent("Test Parent", "1234 Parent St.", "Test City", "CA", "98765"),
      Contact("Test Contact", "123-456-7890", "987-654-3210", "test@contact.org")
    )

    "succeed when TS's activity year matches the filing year" in {
      val ctx = ValidationContext(None, Some(2017))

      S100.inContext(ctx)(ts) mustBe Success()
    }

    "fail when TS's activity year does not match the filing year" in {
      val ctx = ValidationContext(None, Some(2018))

      S100.inContext(ctx)(ts) mustBe Failure()
    }
  }
}

