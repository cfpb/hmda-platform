package hmda.model.institution

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.{ Depository, NonDepository }
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import org.scalatest.{ MustMatchers, WordSpec }

class InstitutionSpec extends WordSpec with MustMatchers {

  "Institution" must {

    val externalIds = Set(
      ExternalId("111111", FdicCertNo),
      ExternalId("222222", FederalTaxId),
      ExternalId("333333", NcuaCharterId),
      ExternalId("444444", OccCharterId),
      ExternalId("555555", RssdId)
    )

    "fail to resolve respondentId when a required externalId is not present" in {
      val inst = createInstitution(Set(ExternalId("666666", FederalTaxId)), CFPB, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Left(RequiredExternalIdNotPresent("1", RssdId))
      expectedId.left.get.message mustBe s"Institution 1 does not have an externalId of type $RssdId"
    }

    "resolve respondentId to RSSD ID when agency is CFPB and depository type is depository" in {
      val inst = createInstitution(externalIds, CFPB, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "resolve respondentId to Federal Tax ID when agency is CFPB and is a non-depository institution" in {
      val inst = createInstitution(externalIds, CFPB, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to FDIC Certificate Number when Agency is FDIC and is a depository institution" in {
      val inst = createInstitution(externalIds, FDIC, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("111111", FdicCertNo))
    }

    "resolve respondentId to Federal Tax ID when Agency is FDIC and is a non-depository institution" in {
      val inst = createInstitution(externalIds, FDIC, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a depository institution" in {
      val inst = createInstitution(externalIds, FRS, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a non-depository institution" in {
      val inst = createInstitution(externalIds, FRS, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "fail to resolve respondentId when Agency is HUD and is a depository institution" in {
      val inst = createInstitution(externalIds, HUD, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Left(UnsupportedDepositoryTypeByAgency("1", HUD, Depository))
      expectedId.left.get.message mustBe "Institution 1 is associated with agency HUD, which does not support depositoryType Depository"
    }

    "resolve respondentId to Federal Tax ID when Agency is HUD and is a non-depository institution" in {
      val inst = createInstitution(externalIds, HUD, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to NCUA Charter ID when Agency is NCUA and is a depository institution" in {
      val inst = createInstitution(externalIds, NCUA, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("333333", NcuaCharterId))
    }

    "resolve respondentId to Federal Tax ID when Agency is NCUA and is a non-depository institution" in {
      val inst = createInstitution(externalIds, NCUA, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to OCC Charter ID when Agency is OCC and is a depository institution" in {
      val inst = createInstitution(externalIds, OCC, Bank)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("444444", OccCharterId))
    }

    "resolve respondentId to Federal Tax ID when Agency is OCC and is a non-depository institution" in {
      val inst = createInstitution(externalIds, OCC, IndependentMortgageCompany)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    def createInstitution(externalIds: Set[ExternalId], agency: Agency, instType: InstitutionType): Institution = {
      Institution("1", "Test Bank", externalIds, agency, instType, hasParent = true, cra = true, status = Active)
    }
  }

}
