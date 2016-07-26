package hmda.model.institution

import enumeratum._
import enumeratum.values.{ IntEnum, IntEnumEntry }

/**
 * Brainstorming how Institution could be modeled.
 */
case class Institution(
    id: Int,
    name: String,
    externalIds: Set[ExternalId],
    agency: Agency,
    institutionType: InstitutionType //,
//    parent: Option[Institution],
//    branches: Option[Set[Institution]]
) {
  val extIdsByType = externalIds.map(extId => (extId.idType, extId)).toMap

  // TODO: Let's figure out if this should fail on instantiation rather than on this call?
  // TODO: Is NIC data of such high quality that these issues don't really happen?
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


sealed abstract class InvalidRespondentId() {
  def message: String
}
case class NoDepositoryTypeForInstitutionType(institutionId: Int, institutionType: InstitutionType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId has an institutionType of $institutionType, which does not have a depositoryType"
}
case class UnsupportedDepositoryTypeByAgency(institutionId: Int, agency: Agency, depositoryType: DepositoryType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId is associated with agency $agency, which does not support depositoryType $depositoryType"
}
case class RequiredExternalIdNotPresent(institutionId: Int, externalIdType: ExternalIdType) extends InvalidRespondentId {
  override def message = s"Institution $institutionId does not have an externalId of type $externalIdType"
}

