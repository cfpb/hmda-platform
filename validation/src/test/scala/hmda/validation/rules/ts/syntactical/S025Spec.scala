package hmda.validation.rules.ts.syntactical

import hmda.model.fi.lar._
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, RssdId, UndeterminedExternalId }
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution.{ EmailDomains, ExternalId, Institution, TopHolder }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.{ MustMatchers, WordSpec }

class S025Spec extends WordSpec with MustMatchers {

  "S025" must {
    "be named S025" in {
      val institution = createInstitution(ExternalId("111111", FdicCertNo))
      val ctx = ValidationContext(Some(institution), None)

      S025.inContext(ctx).name mustBe "S025"
    }
  }

  "S025 for TS" must {

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

    "succeed when TS's agency code and respondent ID match the Institution's" in {
      val institution = createInstitution(ExternalId("999999", RssdId))
      val ctx = ValidationContext(Some(institution), None)

      S025.inContext(ctx)(ts) mustBe Success()
    }

    "fail when TS's agency code and respondent ID do NOT match the Institution's" in {
      val institution = createInstitution(ExternalId("111111", RssdId))
      val ctx = ValidationContext(Some(institution), None)

      S025.inContext(ctx)(ts) mustBe Failure()
    }
  }

  "S025 for LAR" must {

    val lar = LoanApplicationRegister(
      2,
      "999999",
      9,
      Loan("12345678", "20170101", 1, 1, 1, 1, 1000000),
      1,
      1,
      1,
      Geography("", "", "", ""),
      Applicant(1, 1, 1, "", "", "", "", 1, "", "", "", "", 1, 1, ""),
      1,
      Denial("", "", ""),
      "",
      1,
      1
    )

    "succeed when institution is not present in ValidationContext" in {
      val ctx = ValidationContext(None, None)

      S025.inContext(ctx).apply(lar) mustBe Success()
    }

    "succeed when LAR's agency code and respondent ID match the Institution's" in {
      val institution = createInstitution(ExternalId("999999", RssdId))
      val ctx = ValidationContext(Some(institution), None)

      S025.inContext(ctx).apply(lar) mustBe Success()
    }

    "fail when LAR's agency code and respondent ID do NOT match the Institution's" in {
      val institution = createInstitution(ExternalId("111111", RssdId))
      val ctx = ValidationContext(Some(institution), None)

      S025.inContext(ctx).apply(lar) mustBe Failure()
    }
  }

  def createInstitution(externalId: ExternalId): Institution = {
    val emails = EmailDomains("", "", "")
    val respondent = hmda.model.institution.Respondent(externalId, "test bank", "", "", "")
    val parent = hmda.model.institution.Parent("123", 123, "test parent", "", "")
    val topHolder = TopHolder(-1, "", "", "", "")
    Institution("1", CFPB, 2017, Bank, cra = true, Set(), emails, respondent, hmdaFilerFlag = true, parent, 0, 0, topHolder)
  }
}

