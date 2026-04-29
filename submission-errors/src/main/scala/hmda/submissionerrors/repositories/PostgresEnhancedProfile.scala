package hmda.submissionerrors.repositories

import com.github.tminglei.slickpg.{ ExPostgresProfile, PgArraySupport, PgSprayJsonSupport }
import slick.basic.Capability
// $COVERAGE-OFF$
trait PostgresEnhancedProfile extends ExPostgresProfile with PgArraySupport with PgSprayJsonSupport {
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api: API = new API {}

  // NOTE: You need to use the super.API syntax otherwise the implicits won't resolve correctly
  trait API extends super.API with ArrayImplicits with JsonImplicits {
    implicit val simpleStrVectorTypeMapper: DriverJdbcType[Vector[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toVector)
  }
}
object PostgresEnhancedProfile extends PostgresEnhancedProfile {
  override def pgjson: String = "jsonb"
}
// $COVERAGE-ON$