package hmda.model.institution

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.Depository
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionType._
import org.scalatest.{MustMatchers, WordSpec}

class InstitutionSpec extends WordSpec with MustMatchers {

  "Institution" must {

    val externalIds = Set(
      ExternalId("111111", FdicCertNo),
      ExternalId("222222", FederalTaxId),
      ExternalId("333333", NcuaCharterId),
      ExternalId("444444", OccCharterId),
      ExternalId("555555", RssdId)
    )

    "fail to resolve respondentId when institution type does not have a depository type" in {
      val inst = Institution(1, externalIds, CFPB, NoDepositTypeInstType)
      val expectedId = inst.respondentId

      expectedId.isLeft mustBe true
      val failType = expectedId.left.get
      failType mustBe NoDepositoryTypeForInstitutionType(1, NoDepositTypeInstType)
      failType.message mustBe s"Institution 1 has an institutionType of $NoDepositTypeInstType, which does not have a depositoryType"
    }

    "fail to resolve respondentId when a required externalId is not present" in {
      val inst = Institution(1, Set(ExternalId("666666", FederalTaxId)), CFPB, Bank)
      val expectedId = inst.respondentId

      expectedId.isLeft mustBe true
      val failType = expectedId.left.get
      failType mustBe RequiredExternalIdNotPresent(1, RssdId)
      failType.message mustBe s"Institution 1 does not have an externalId of type $RssdId"
    }

    "resolve respondentId to RSSD ID when agency is CFPB and depository type is depository" in {
      val inst = Institution(1, externalIds, CFPB, Bank)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("555555", RssdId)
    }

    "resolve respondentId to Federal Tax ID when agency is CFPB and is a non-depository institution" in {
      val inst = Institution(1, externalIds, CFPB, NonDepositInstType)
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
      val inst = Institution(1, externalIds, FDIC, NonDepositInstType)
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
      val inst = Institution(1, externalIds, FRS, NonDepositInstType)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("555555", RssdId)
    }

    "fail to resolve respondentId when Agency is HUD and is a depository institution" in {
      val inst = Institution(1, externalIds, HUD, Bank)
      val expectedId = inst.respondentId

      expectedId.isLeft mustBe true
      val failType = expectedId.left.get
      failType mustBe UnsupportedDepositoryTypeByAgency(1, HUD, Depository)
      failType.message mustBe "Institution 1 is associated with agency HUD, which does not support depositoryType Depository"
    }

    "resolve respondentId to Federal Tax ID when Agency is HUD and is a non-depository institution" in {
      val inst = Institution(1, externalIds, HUD, NonDepositInstType)
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
      val inst = Institution(1, externalIds, NCUA, NonDepositInstType)
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
      val inst = Institution(1, externalIds, OCC, NonDepositInstType)
      val expectedId = inst.respondentId

      expectedId.isRight mustBe true
      expectedId.right.get mustBe ExternalId("222222", FederalTaxId)
    }

  }

}
