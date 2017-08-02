package hmda.model.institution

import hmda.model.institution.Agency.UndeterminedAgency
import hmda.model.institution.ExternalIdType.UndeterminedExternalId
import hmda.model.institution.InstitutionType.UndeterminedInstitutionType

/**
 * A financial institution, geared towards requirements for filing HMDA data.
 */
case class Institution(
  id: String,
  agency: Agency,
  activityYear: Int,
  institutionType: InstitutionType,
  cra: Boolean,
  externalIds: Set[ExternalId],
  emailDomains: Set[String],
  respondent: Respondent,
  hmdaFilerFlag: Boolean,
  parent: Parent,
  assets: Int,
  otherLenderCode: Int,
  topHolder: TopHolder
)
case object Institution {
  def empty: Institution = Institution(
    "",
    UndeterminedAgency,
    -1,
    UndeterminedInstitutionType,
    cra = false,
    Set(),
    Set(),
    Respondent(),
    hmdaFilerFlag = false,
    Parent("", -1, "", "", ""),
    -1,
    -1,
    TopHolder(-1, "", "", "", "")
  )
}

case class Respondent(
  externalId: ExternalId = ExternalId("", UndeterminedExternalId),
  name: String = "",
  state: String = "",
  city: String = "",
  fipsStateNumber: String = ""
)
case class Parent(respondentId: String, idRssd: Int, name: String, city: String, state: String)
case class TopHolder(idRssd: Int, name: String, city: String, state: String, country: String)

sealed abstract class InvalidRespondentId {
  def message: String
}

case class UnsupportedDepositoryTypeByAgency(institutionId: String, agency: Agency, depositoryType: DepositoryType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId is associated with agency $agency, which does not support depositoryType $depositoryType"
}
case class RequiredExternalIdNotPresent(institutionId: String, externalIdType: ExternalIdType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId does not have an externalId of type $externalIdType"
}

