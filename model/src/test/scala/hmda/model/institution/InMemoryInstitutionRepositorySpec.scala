package hmda.model.institution

import hmda.model.institution.Agency.{ CFPB, FDIC, OCC }
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, OccCharterId, RssdId }
import hmda.model.institution.InstitutionType.{ Bank, SavingsAndLoan }
import org.scalatest.{ MustMatchers, WordSpec }

class InMemoryInstitutionRepositorySpec extends WordSpec with MustMatchers {

  val institutionRepository = new InMemoryInstitutionRepository(Set(
    Institution(1, "Test Bank 1", Set(ExternalId("99-1234567", FederalTaxId), ExternalId("123456", RssdId)), CFPB, Bank),
    Institution(2, "Test Bank 2", Set(ExternalId("98-1234567", FederalTaxId), ExternalId("9898989", FdicCertNo)), FDIC, Bank),
    Institution(3, "Test Bank 3", Set(ExternalId("97-1234567", FederalTaxId), ExternalId("64646464", OccCharterId)), OCC, SavingsAndLoan)
  ))

  "InMemoryInstitutionRepository" must {

    "return None if an institution is not found by ID" in {
      institutionRepository.get(4) mustBe None
    }

    "return None if an institution is not found by ExternalId" in {
      institutionRepository.findByExternalId(ExternalId("1234567", FederalTaxId)) mustBe None
    }

    "return the correct institution when retrieved by ID" in {
      institutionRepository.get(1) mustBe Some(
        Institution(1, "Test Bank 1", Set(ExternalId("99-1234567", FederalTaxId), ExternalId("123456", RssdId)), CFPB, Bank)
      )
    }

    "return the correct institution when retrieved by ExternalId" in {
      institutionRepository.findByExternalId(ExternalId("98-1234567", FederalTaxId)) mustBe Some(
        Institution(2, "Test Bank 2", Set(ExternalId("98-1234567", FederalTaxId), ExternalId("9898989", FdicCertNo)), FDIC, Bank)
      )
    }

  }

}
