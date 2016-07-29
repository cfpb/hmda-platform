package hmda.model.institution

import hmda.model.institution.Agency._
import hmda.model.institution.DepositoryType.Depository
import hmda.model.institution.ExternalIdType._
import hmda.model.institution.InstitutionStatus.Active
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

    "fail to resolve respondentId when institution type does not have a depository type" in {
      val inst = Institution(1, "Test Bank", externalIds, CFPB, NoDepositTypeInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Left(NoDepositoryTypeForInstitutionType(1, NoDepositTypeInstType))
      expectedId.left.get.message mustBe s"Institution 1 has an institutionType of $NoDepositTypeInstType, which does not have a depositoryType"
    }

    "fail to resolve respondentId when a required externalId is not present" in {
      val inst = Institution(1, "Test Bank", Set(ExternalId("666666", FederalTaxId)), CFPB, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Left(RequiredExternalIdNotPresent(1, RssdId))
      expectedId.left.get.message mustBe s"Institution 1 does not have an externalId of type $RssdId"
    }

    "resolve respondentId to RSSD ID when agency is CFPB and depository type is depository" in {
      val inst = Institution(1, "Test Bank", externalIds, CFPB, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "resolve respondentId to Federal Tax ID when agency is CFPB and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, CFPB, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to FDIC Certificate Number when Agency is FDIC and is a depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, FDIC, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("111111", FdicCertNo))
    }

    "resolve respondentId to Federal Tax ID when Agency is FDIC and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, FDIC, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, FRS, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "resolve respondentId to RSSD ID when Agency is FRS and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, FRS, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("555555", RssdId))
    }

    "fail to resolve respondentId when Agency is HUD and is a depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, HUD, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Left(UnsupportedDepositoryTypeByAgency(1, HUD, Depository))
      expectedId.left.get.message mustBe "Institution 1 is associated with agency HUD, which does not support depositoryType Depository"
    }

    "resolve respondentId to Federal Tax ID when Agency is HUD and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, HUD, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to NCUA Charter ID when Agency is NCUA and is a depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, NCUA, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("333333", NcuaCharterId))
    }

    "resolve respondentId to Federal Tax ID when Agency is NCUA and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, NCUA, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }

    "resolve respondentId to OCC Charter ID when Agency is OCC and is a depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, OCC, Bank, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("444444", OccCharterId))
    }

    "resolve respondentId to Federal Tax ID when Agency is OCC and is a non-depository institution" in {
      val inst = Institution(1, "Test Bank", externalIds, OCC, NonDepositInstType, Active)
      val expectedId = inst.respondentId

      expectedId mustBe Right(ExternalId("222222", FederalTaxId))
    }
  }

}
