package hmda.model.institution

/**
 * Datastore for [[hmda.model.institution.Institution]]s
 */
trait InstitutionRepository {

  def get(id: Integer): Option[Institution]

  def findByExternalId(externalId: ExternalId): Option[Institution]
}

/**
 * In-memory implementation of [[hmda.model.institution.InstitutionRepository]]
 */
class InMemoryInstitutionRepository(institutions: Set[Institution]) extends InstitutionRepository {

  val instById: Map[Int, Institution] = institutions.map(i => (i.id, i)).toMap

  val instByExtId: Map[ExternalId, Institution] = institutions.flatMap(i =>
    i.externalIds.map(extId => (extId, i))).toMap

  override def get(id: Integer): Option[Institution] = instById.get(id)

  override def findByExternalId(externalId: ExternalId): Option[Institution] = instByExtId.get(externalId)
}