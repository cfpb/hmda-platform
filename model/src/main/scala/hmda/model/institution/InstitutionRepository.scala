package hmda.model.institution

/**
  * Created by keelerh on 7/26/16.
  */
trait InstitutionRepository {

  def get(id: Integer): Option[Institution]

  def findByExternalId(externalId: ExternalId): Option[Institution]
}

class InMemoryInstitutionRepository(institutions: Set[Institution]) extends InstitutionRepository{

  val instById: Map[Int, Institution] = institutions.map(i => (i.id, i)).toMap

  val instByExtId: Map[ExternalId, Institution] = institutions.flatMap(i =>
    i.externalIds.map(extId => (extId, i))).toMap

  override def get(id: Integer): Option[Institution] = instById.get(id)

  override def findByExternalId(externalId: ExternalId): Option[Institution] = instByExtId.get(externalId)
}