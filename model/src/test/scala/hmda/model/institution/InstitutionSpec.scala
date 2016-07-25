package hmda.model.institution

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType._
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import org.scalatest.{ MustMatchers, WordSpec }

/**
 * Created by keelerh on 7/22/16.
 */
class InstitutionSpec extends WordSpec with MustMatchers {

  "Institution" must {

    val externalIds = Set(
      ExternalId("111111", FdicCertNo),
      ExternalId("222222", FederalTaxId),
      ExternalId("333333", NcuaCharterId),
      ExternalId("444444", OccCharterId),
      ExternalId("555555", RssdId)
    )

    "resolve respondentId to RSSD ID when agency is CFPB and depository type is depository" in {
      val inst = Institution(1, externalIds, CFPB, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("555555", RssdId)
    }

    "resolve respondentId to Federal Tax ID when agency is CFPB and is a non-depository institution" in {
      val inst = Institution(1, externalIds, CFPB, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

    "resolve respondentId to FDIC Certificate Number when Agency is FDIC and is a depository institution" in {
      val inst = Institution(1, externalIds, FDIC, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("111111", FdicCertNo)
    }

    "resolve respondentId to Federal Tax ID when Agency is FDIC and is a non-depository institution" in {
      val inst = Institution(1, externalIds, FDIC, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a depository institution" in {
      val inst = Institution(1, externalIds, FRS, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("555555", RssdId)
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a non-depository institution" in {
      val inst = Institution(1, externalIds, FRS, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("555555", RssdId)
    }

    "fail to resolve respondentId when Agency is HUD and is a depository institution" in {
      val inst = Institution(1, externalIds, HUD, Bank)
      val expectedId = inst.respondentId

      expectedId.isLeft mustBe true
      expectedId.left.get.message mustBe
        "Institution 1 is associated with agency HUD, which does not support depositoryType 'Depository'"
    }

    "resolve respondentId to Federal Tax ID when Agency is HUD and is a non-depository institution" in {
      val inst = Institution(1, externalIds, HUD, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

    "resolve respondentId to NCUA Charter ID when Agency is NCUA and is a depository institution" in {
      val inst = Institution(1, externalIds, NCUA, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("333333", NcuaCharterId)
    }

    "resolve respondentId to Federal Tax ID when Agency is NCUA and is a non-depository institution" in {
      val inst = Institution(1, externalIds, NCUA, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

    "resolve respondentId to OCC Charter ID when Agency is OCC and is a depository institution" in {
      val inst = Institution(1, externalIds, OCC, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("444444", OccCharterId)
    }

    "resolve respondentId to Federal Tax ID when Agency is OCC and is a non-depository institution" in {
      val inst = Institution(1, externalIds, OCC, MortgateBankingSubsidiary)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

  }

}
