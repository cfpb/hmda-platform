package hmda.model.institution

/**
 * A financial institution, geared towards requirements for filing HMDA data.
 */
case class Institution(
    id: String,
    name: String,
    externalIds: Set[ExternalId],
    agency: Agency,
    institutionType: InstitutionType,
    hasParent: Boolean,
    cra: Boolean = false, // TODO do we have this info when creating the institution? if so, then don't default here.
    status: InstitutionStatus = InstitutionStatus.Active
) extends Serializable {

  private val extIdsByType: Map[ExternalIdType, ExternalId] = externalIds.map(extId => (extId.idType, extId)).toMap

  /**
   * Derives the respondentId for a given Institution based on [[hmda.model.institution.Agency]] and [[hmda.model.institution.InstitutionType]],
   * the rules for which can be found in section "1.4 - Respondent Identification Numbers for 2017 HMDA Filers" of the
   * <a href="http://www.consumerfinance.gov/data-research/hmda/static/for-filers/2017/2017-HMDA-File-Specifications.pdf">2017 HMDA File Specifications</a>
   */
  def respondentId: Either[InvalidRespondentId, ExternalId] = {

    institutionType.depositoryType match {
      case None => Left(NoDepositoryTypeForInstitutionType(id, institutionType))
      case Some(dt) =>

        agency.externalIdTypes.get(dt) match {
          case None => Left(UnsupportedDepositoryTypeByAgency(id, agency, dt))
          case Some(extIdType) =>

            extIdsByType.get(extIdType) match {
              case None => Left(RequiredExternalIdNotPresent(id, extIdType))
              case Some(extId) => Right(ExternalId(extId.id, extIdType))
            }
        }
    }
  }

}

sealed abstract class InvalidRespondentId {
  def message: String
}
case class NoDepositoryTypeForInstitutionType(institutionId: String, institutionType: InstitutionType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId has an institutionType of $institutionType, which does not have a depositoryType"
}
case class UnsupportedDepositoryTypeByAgency(institutionId: String, agency: Agency, depositoryType: DepositoryType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId is associated with agency $agency, which does not support depositoryType $depositoryType"
}
case class RequiredExternalIdNotPresent(institutionId: String, externalIdType: ExternalIdType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId does not have an externalId of type $externalIdType"
}

