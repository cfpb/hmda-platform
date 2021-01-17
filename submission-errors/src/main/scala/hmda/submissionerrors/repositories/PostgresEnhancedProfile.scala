package hmda.submissionerrors.repositories

import com.github.tminglei.slickpg.{ ExPostgresProfile, PgArraySupport }
import slick.basic.Capability
// $COVERAGE-OFF$
trait PostgresEnhancedProfile extends ExPostgresProfile with PgArraySupport {
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api: API = new API {}

  // NOTE: You need to use the super.API syntax otherwise the implicits won't resolve correctly
  trait API extends super.API with ArrayImplicits {
    implicit val simpleStrVectorTypeMapper: DriverJdbcType[Vector[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toVector)
  }
}
object PostgresEnhancedProfile extends PostgresEnhancedProfile
// $COVERAGE-ON$