package hmda.model.institution

import enumeratum._
import enumeratum.values.{ IntEnum, IntEnumEntry }

/**
 * Brainstorming how Institution could be modeled.
 */
case class Institution(
    id: Int,
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
      case None => Left(InvalidRespondentId(
        s"Institution $id has an institutionType of $institutionType, which does not have a depositoryType"
      ))
      case Some(dt) =>

        agency.externalIdTypes.get(dt) match {
          case None => Left(InvalidRespondentId(
            s"Institution $id is associated with agency $agency, which does not support depositoryType '$dt'"
          ))
          case Some(extIdType) =>

            extIdsByType.get(extIdType) match {
              case None => Left(InvalidRespondentId(
                s"Institution $id does not have an externalId of type '$extIdType'"
              ))
              case Some(extId) => Right(ExternalId(extId.id, extIdType))
            }
        }
    }

  }

}

// TODO: Consider making this a sealed trait with failure type impls
case class InvalidRespondentId(message: String)

class InstitutionRepository(institutions: Set[Institution]) {

  val instById: Map[Int, Institution] = institutions.map(i => (i.id, i)).toMap

  val instByExtId: Map[ExternalId, Institution] = institutions.flatMap(i =>
    i.externalIds.map(extId => (extId, i))).toMap

  def get(id: Integer): Option[Institution] = instById.get(id)

  def findByExternalId(externalId: ExternalId): Option[Institution] = instByExtId.get(externalId)

}
