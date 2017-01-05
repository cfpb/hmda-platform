package hmda.model.institution

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
    emailDomains: EmailDomains,
    respondent: Respondent,
    hmdaFilerFlag: Boolean,
    parent: Parent,
    assets: Int,
    otherLenderCode: Int,
    topHolder: TopHolder
)

case class EmailDomains(email2015: String, email2014: String, email2013: String)
case class Respondent(id: ExternalId, name: String, state: String, city: String, fipsStateNumber: String)
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

