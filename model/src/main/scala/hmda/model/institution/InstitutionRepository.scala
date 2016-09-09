package hmda.model.institution

/**
 * Datastore for [[hmda.model.institution.Institution]]s
 */
trait InstitutionRepository {

  def get(id: String): Option[Institution]

}

/**
 * In-memory implementation of [[hmda.model.institution.InstitutionRepository]]
 */
class InMemoryInstitutionRepository(institutions: Set[Institution]) extends InstitutionRepository {

  private val instById: Map[String, Institution] = institutions.map(i => (i.id, i)).toMap

  override def get(id: String): Option[Institution] = instById.get(id)

}
